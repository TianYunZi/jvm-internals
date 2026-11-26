package org.example.jvminternals.ch12;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** 固定规模展示本地资源拥有者与持有者，留出三个限时诊断窗口。 */
public final class NativeJourney {
    private NativeJourney() {
    }

    /** 参数为隔离Disposable类目录；三阶段后自然退出，不制造资源耗尽。 */
    public static void main(String[] args) throws IOException, ReflectiveOperationException, InterruptedException {
        if (args.length != 1) {
            throw new IllegalArgumentException("需要一个隔离类目录参数");
        }
        System.out.println("进程=" + ProcessHandle.current().pid());
        report("基线");
        Thread.sleep(45_000);
        try (Resources resources = new Resources()) {
            resources.populate(Path.of(args[0]));
            System.out.println("持有：切片容量=" + resources.view.capacity()
                    + "；外部块=" + resources.segment.byteSize()
                    + "；隔离实例=" + resources.instances.size());
            report("持有");
            Thread.sleep(45_000);
        }
        // 显式关闭Arena与停止线程已完成；直接缓冲区和类卸载还需收集协作。
        System.gc();
        report("释放请求后");
        Thread.sleep(45_000);
        System.gc();
        report("结束");
    }

    private static void report(String phase) {
        System.out.println("阶段=" + phase + "；时间=" + Instant.now()
                + "；已加载类=" + ManagementFactory.getClassLoadingMXBean().getLoadedClassCount()
                + "；已卸载类=" + ManagementFactory.getClassLoadingMXBean().getUnloadedClassCount()
                + "；Java活线程=" + ManagementFactory.getThreadMXBean().getThreadCount());
        for (BufferPoolMXBean pool : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)) {
            if ("direct".equals(pool.getName())) {
                System.out.println("直接池：数量=" + pool.getCount() + "；容量=" + pool.getTotalCapacity()
                        + "；内存=" + pool.getMemoryUsed());
            }
        }
    }

    // 资源管理对象只在主线程使用，关闭覆盖正常与异常退出路径。
    private static final class Resources implements AutoCloseable {
        private final Arena arena = Arena.ofConfined();
        private final CountDownLatch stop = new CountDownLatch(1);
        private final List<Thread> workers = new ArrayList<>();
        private final List<Object> instances = new ArrayList<>();
        private ByteBuffer view;
        private MemorySegment segment;

        private void populate(Path classDirectory) throws IOException, ReflectiveOperationException {
            ByteBuffer owner = ByteBuffer.allocateDirect(8 * 1024 * 1024);
            view = owner.slice(0, 1);
            segment = arena.allocate(256 * 1024);
            segment.fill((byte) 1);
            URL input = classDirectory.toUri().toURL();
            for (int i = 0; i < 24; i++) {
                try (URLClassLoader loader = new URLClassLoader(new URL[]{input}, ClassLoader.getPlatformClassLoader())) {
                    instances.add(loader.loadClass("org.example.jvminternals.ch08.disposable.Disposable")
                            .getDeclaredConstructor().newInstance());
                }
            }
            for (int i = 0; i < 4; i++) {
                Thread worker = Thread.ofPlatform().name("native-holder-" + i).unstarted(this::awaitStop);
                workers.add(worker);
                worker.start();
            }
        }

        private void awaitStop() {
            try {
                // 即使主线程未正常清理，工作线程也有独立退出上限。
                boolean released = stop.await(90, TimeUnit.SECONDS);
                if (!released) {
                    System.out.println("教学线程等待达到上限，结束");
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                System.out.println("教学线程被中断，结束等待");
            }
        }

        @Override
        public void close() throws InterruptedException {
            stop.countDown();
            try {
                for (Thread worker : workers) {
                    worker.join(1_000);
                }
            } finally {
                // 清掉切片和实例持有链；不使用内部强制清理接口。
                view = null;
                instances.clear();
                arena.close();
            }
        }
    }
}
