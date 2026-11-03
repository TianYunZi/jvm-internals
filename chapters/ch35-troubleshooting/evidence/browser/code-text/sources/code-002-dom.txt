package org.example.jvminternals.ch35;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/** 明确授权的故障教学：容量、时间与等待均有上限，不耗尽资源。 */
public final class BoundedSymptoms {
    private static final ReentrantLock LEFT = new ReentrantLock();
    private static final ReentrantLock RIGHT = new ReentrantLock();
    private static volatile long consumed;

    private BoundedSymptoms() {
    }

    /** 三个模式分别运行，避免用一个时点的不同现象互相归因。 */
    public static void main(String[] args) throws InterruptedException {
        System.out.printf("目标 pid=%d mode=%s time=%s%n",
                ProcessHandle.current().pid(), args[0], Instant.now());
        switch (args[0]) {
            case "memory" -> memory();
            case "cpu" -> cpu();
            case "locks" -> locks();
            default -> throw new IllegalArgumentException("模式仅支持memory、cpu、locks");
        }
        System.out.println("有界案例完成");
    }

    private static void memory() throws InterruptedException {
        List<byte[]> retained = new ArrayList<>();
        Thread.sleep(2_000);
        for (int index = 0; index < 24; index++) {
            retained.add(new byte[256 * 1024]);
        }
        System.out.println("持有窗口 bytes=6291456");
        Thread.sleep(5_000);
        retained.clear();
        System.out.println("引用已释放；是否回收由后续诊断观察");
        Thread.sleep(5_000);
    }

    private static void cpu() throws InterruptedException {
        Thread.sleep(1_000);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        long value = 7;
        do {
            for (int index = 0; index < 10_000; index++) {
                value = value * 1_664_525 + 1_013_904_223;
            }
            consumed = value;
        } while (System.nanoTime() < deadline);
        System.out.println("计算窗口结束 checksum=" + consumed);
    }

    private static void locks() throws InterruptedException {
        CountDownLatch bothHeld = new CountDownLatch(2);
        Thread first = Thread.ofPlatform().name("left-to-right").start(() -> waitFor(LEFT, RIGHT, bothHeld));
        Thread second = Thread.ofPlatform().name("right-to-left").start(() -> waitFor(RIGHT, LEFT, bothHeld));
        first.join();
        second.join();
    }

    private static void waitFor(ReentrantLock first, ReentrantLock second, CountDownLatch bothHeld) {
        first.lock();
        try {
            bothHeld.countDown();
            bothHeld.await();
            // 有意建立逆序等待，超时打破环；不把超时退出称作根因修复。
            if (second.tryLock(4, TimeUnit.SECONDS)) {
                try {
                    System.out.println("取得第二把锁 thread=" + Thread.currentThread().getName());
                } finally {
                    second.unlock();
                }
            } else {
                System.out.println("等待超时，释放第一把锁 thread=" + Thread.currentThread().getName());
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            System.err.println("等待中断，按所有权释放锁");
        } finally {
            first.unlock();
        }
    }
}
