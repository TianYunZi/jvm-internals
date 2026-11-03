package org.example.jvminternals.ch01;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** CH01-E01：贯穿全书的正常程序，展示对象、方法、文件读取和有界线程协作。 */
public final class ProgramJourney {
    private static final System.Logger LOGGER = System.getLogger(ProgramJourney.class.getName());
    private static final long START_NANOS = System.nanoTime();

    private ProgramJourney() {
    }

    /**
     * 接收输入文件和 0—60 秒观察窗口；主线程与工作线程都具有结束上限。
     * 相对时间从本类初始化算起，不代表进程从启动器开始的启动耗时。
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 2) {
            throw new IllegalArgumentException("用法：ProgramJourney <文本文件> <观察秒数 0—60>");
        }
        int observationSeconds = Integer.parseInt(args[1]);
        if (observationSeconds < 0 || observationSeconds > 60) {
            throw new IllegalArgumentException("观察秒数必须位于 0—60，避免教学进程长期占用资源");
        }
        LOGGER.log(System.Logger.Level.INFO, "贯穿程序启动，观察窗口为 {0} 秒", observationSeconds);
        Runtime.getRuntime().addShutdownHook(Thread.ofPlatform().name("journey-shutdown")
                .unstarted(() -> phase("shutdown-hook")));
        phase("main-enter");
        String text = Files.readString(Path.of(args[0]), StandardCharsets.UTF_8);
        Payload payload = new Payload(text);
        phase("object-created");
        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread worker = Thread.ofPlatform().name("journey-worker")
                .unstarted(() -> process(payload, ready, release, observationSeconds));
        worker.start();
        try {
            if (!ready.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("工作线程未在教学准备期限内就绪");
            }
            System.out.printf("pid=%d observation_seconds=%d%n", ProcessHandle.current().pid(), observationSeconds);
            phase("observation-window");
            // 此等待留出诊断工具采集窗口；到期自然继续，不以休眠推测工作完成。
            release.await(observationSeconds, TimeUnit.SECONDS);
        } finally {
            release.countDown();
            if (!worker.join(Duration.ofSeconds(5))) {
                worker.interrupt();
                worker.join(Duration.ofSeconds(5));
            }
        }
        phase("main-return");
        LOGGER.log(System.Logger.Level.INFO, "贯穿程序结束，工作线程存活状态为 {0}", worker.isAlive());
    }

    private static void process(Payload payload, CountDownLatch ready, CountDownLatch release,
                                int observationSeconds) {
        phase("worker-enter");
        System.out.println("payload=" + payload.describe());
        phase("worker-ready");
        ready.countDown();
        try {
            // 即使主线程异常离开，工作线程的等待也有独立上限。
            release.await(observationSeconds + 5L, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LOGGER.log(System.Logger.Level.WARNING, "工作线程收到中断，结束观察等待");
        }
        phase("worker-return");
    }

    private static void phase(String name) {
        System.out.printf("elapsed_ms=%d thread=%s stage=%s%n",
                (System.nanoTime() - START_NANOS) / 1_000_000, Thread.currentThread().getName(), name);
    }

    /** 普通对象及其持有的小数组，供后续章节关联对象与线程的持有关系。 */
    private static final class Payload {
        private final String text;
        private final byte[] workspace = new byte[1024];

        private Payload(String text) {
            this.text = text;
        }

        private String describe() {
            return text.strip() + " workspace_bytes=" + workspace.length;
        }
    }
}
