package org.example.jvminternals.ch07;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** 两个线程请求同一类初始化；门闩只用于形成有界的人工观察窗口。 */
public final class InitializationRace {
    private static final System.Logger LOG = System.getLogger(InitializationRace.class.getName());
    private static final CountDownLatch ENTERED = new CountDownLatch(1);
    private static final CountDownLatch RELEASE = new CountDownLatch(1);
    private static final CountDownLatch REQUESTED = new CountDownLatch(1);
    private static int holdSeconds;

    private InitializationRace() { }

    static final class Lazy {
        static final int VALUE = initialize();

        private static int initialize() {
            LOG.log(System.Logger.Level.INFO, "初始化所有者进入，等待观察窗口结束");
            ENTERED.countDown();
            try {
                if (!RELEASE.await(holdSeconds + 5L, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("观察窗口未按时释放");
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("初始化线程被中断", exception);
            }
            return 73;
        }
    }

    private static void awaitPhase(CountDownLatch phase) throws InterruptedException {
        if (!phase.await(3, TimeUnit.SECONDS)) {
            throw new IllegalStateException("教学进程未及时到达预定阶段");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        holdSeconds = args.length == 0 ? 0 : Integer.parseInt(args[0]);
        if (holdSeconds < 0 || holdSeconds > 30) {
            throw new IllegalArgumentException("观察窗口须为 0—30 秒");
        }
        Thread owner = Thread.ofPlatform().name("init-owner").unstarted(
                () -> System.out.println("owner.value=" + Lazy.VALUE));
        Thread contender = Thread.ofPlatform().name("init-contender").unstarted(() -> {
            REQUESTED.countDown();
            System.out.println("contender.value=" + Lazy.VALUE);
        });
        LOG.log(System.Logger.Level.INFO, "开始有界并发初始化观察");
        try {
            owner.start();
            awaitPhase(ENTERED);
            contender.start();
            awaitPhase(REQUESTED);
            // 此标记仅说明即将执行读取；实际等待状态需外部线程快照佐证。
            System.out.println("observe.pid=" + ProcessHandle.current().pid() + " seconds=" + holdSeconds);
            RELEASE.await(holdSeconds, TimeUnit.SECONDS);
        } finally {
            RELEASE.countDown();
            owner.join(5_000);
            contender.join(5_000);
            if (owner.isAlive() || contender.isAlive()) {
                owner.interrupt();
                contender.interrupt();
                LOG.log(System.Logger.Level.ERROR, "工作线程未按时收尾，已请求中断");
            }
        }
        LOG.log(System.Logger.Level.INFO, "有界并发初始化观察结束");
    }
}
