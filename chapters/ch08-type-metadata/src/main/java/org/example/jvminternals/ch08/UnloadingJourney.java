package org.example.jvminternals.ch08;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

/** 先持有实例再释放；只请求两次回收，记录实际结果而不要求卸载必然发生。 */
public final class UnloadingJourney {
    private static final System.Logger LOG = System.getLogger(UnloadingJourney.class.getName());
    private static volatile Object retained;
    private UnloadingJourney() { }

    private static WeakReference<ClassLoader> createAndRetain(Path directory,
            ReferenceQueue<ClassLoader> queue) throws IOException, ReflectiveOperationException {
        URL url = directory.toUri().toURL();
        try (URLClassLoader loader = new URLClassLoader("disposable", new URL[] {url},
                ClassLoader.getPlatformClassLoader())) {
            Class<?> type = Class.forName("org.example.jvminternals.ch08.disposable.Disposable", true, loader);
            retained = type.getConstructor().newInstance();
            System.out.println("retained.class=" + retained.getClass().getName()
                    + " loader=" + loader.getName());
            return new WeakReference<>(loader, queue);
        }
        // 返回后本方法的 Class 与加载器局部变量离开栈帧，资源已关闭，实例仍被静态字段持有。
    }

    private static void printUsage(MemoryPoolMXBean pool, String phase) {
        var usage = pool.getUsage();
        System.out.println(phase + " metaspace.used=" + usage.getUsed()
                + " committed=" + usage.getCommitted());
    }

    public static void main(String[] args) throws IOException, ReflectiveOperationException, InterruptedException {
        if (args.length != 1) {
            throw new IllegalArgumentException("用法：UnloadingJourney <隔离类输出目录>");
        }
        LOG.log(System.Logger.Level.INFO, "开始有限加载器持有与释放观察");
        MemoryPoolMXBean pool = ManagementFactory.getMemoryPoolMXBeans().stream()
                .filter(bean -> "Metaspace".equals(bean.getName())).findFirst().orElseThrow();
        printUsage(pool, "before.load");
        ReferenceQueue<ClassLoader> queue = new ReferenceQueue<>();
        WeakReference<ClassLoader> reference = createAndRetain(Path.of(args[0]), queue);
        printUsage(pool, "after.load");
        System.gc();
        System.out.println("after.held.request loader.cleared=" + reference.refersTo(null));
        printUsage(pool, "after.held.request");
        retained = null;
        LOG.log(System.Logger.Level.INFO, "已释放教学静态持有者，仅再请求一次回收");
        System.gc();
        // 队列等待至多两秒；没有事件也是有效观察，不反复制造分配负载。
        System.out.println("after.release loader.cleared=" + reference.refersTo(null)
                + " queue.event=" + (queue.remove(2_000) != null));
        printUsage(pool, "after.release");
        LOG.log(System.Logger.Level.INFO, "加载器持有与释放观察结束");
    }
}
