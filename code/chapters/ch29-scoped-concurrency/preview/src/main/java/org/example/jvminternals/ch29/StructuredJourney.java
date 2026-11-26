package org.example.jvminternals.ch29;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeUnit;

/** JDK 25第五次预览：任务继承上下文，主动失败与协作取消独立于普通编译。 */
public final class StructuredJourney {
    private static final ScopedValue<String> REQUEST = ScopedValue.newInstance();

    private StructuredJourney() { }

    /** 不带参数为成功路径；fail显式选择已授权的主动异常案例。 */
    public static void main(String[] args) throws InterruptedException {
        boolean fail = args.length == 1 && args[0].equals("fail");
        ScopedValue.where(REQUEST, "request-29").call(() -> {
            runScope(fail);
            return null;
        });
        System.out.println("绑定退出：isBound=" + REQUEST.isBound());
    }

    private static void runScope(boolean fail) throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        try (var scope = StructuredTaskScope.open(
                StructuredTaskScope.Joiner.<String>awaitAllSuccessfulOrThrow(),
                config -> config.withName("request-scope").withTimeout(Duration.ofSeconds(5)))) {
            var slow = scope.fork(() -> {
                System.out.println("子任务slow上下文=" + REQUEST.get());
                started.countDown();
                try {
                    Thread.sleep(fail ? 3_000L : 100L);
                    return "slow-result";
                } catch (InterruptedException interrupted) {
                    System.out.println("子任务slow收到中断");
                    throw interrupted;
                } finally {
                    System.out.println("子任务slow finally退出");
                }
            });
            var fast = scope.fork(() -> {
                started.await(1, TimeUnit.SECONDS);
                System.out.println("子任务fast上下文=" + REQUEST.get());
                if (fail) {
                    System.out.println("子任务fast主动抛出教学异常");
                    throw new IllegalStateException("教学故障：fast");
                }
                return "fast-result";
            });
            try {
                scope.join();
                System.out.println("汇合结果：" + slow.get() + "," + fast.get());
            } finally {
                System.out.println("拥有者即将close");
            }
        } catch (StructuredTaskScope.FailedException failed) {
            System.out.println("close完成后接收失败：" + failed.getCause());
        }
        System.out.println("任务作用域已关闭");
    }
}
