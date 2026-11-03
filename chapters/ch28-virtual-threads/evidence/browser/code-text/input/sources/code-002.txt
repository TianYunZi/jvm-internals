package org.example.jvminternals.ch28;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** 单载体下对照可卸载的同步阻塞和带本地帧的固定；不是吞吐基准。 */
public final class VirtualJourney {
    private static final Object MONITOR = new Object();

    private VirtualJourney() { }

    private static native void callBack();

    /** 由本地代码回调；本地帧仍在栈上，sleep可能无法卸载。 */
    public static void pauseWithinNative() throws InterruptedException {
        System.out.println("本地回调开始：" + Thread.currentThread());
        Thread.sleep(4_000L);
        System.out.println("本地回调结束：" + Thread.currentThread());
    }

    /** monitor或native两种固定输入，全部线程最终join。 */
    public static void main(String[] args) throws InterruptedException {
        boolean nativeMode = args.length == 1 && args[0].equals("native");
        if (nativeMode) {
            System.loadLibrary("carrierjourney");
        }
        CountDownLatch entered = new CountDownLatch(1);
        Thread blocker = Thread.ofVirtual().name("virtual-blocker").start(() -> {
            try {
                synchronized (MONITOR) {
                    System.out.println("阻塞开始：" + Thread.currentThread());
                    entered.countDown();
                    if (nativeMode) {
                        callBack();
                    } else {
                        Thread.sleep(6_000L);
                    }
                    System.out.println("阻塞结束：" + Thread.currentThread());
                }
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                System.out.println("阻塞线程响应中断并退出");
            }
        });
        entered.await(2, TimeUnit.SECONDS);
        // 独立平台主线程延迟派生，避免探针在阻塞前已经完成。
        Thread.sleep(400L);
        Thread probe = Thread.ofVirtual().name("virtual-probe").start(() ->
                System.out.println("探针执行：" + Thread.currentThread()));
        System.out.printf("观察窗口：pid=%d mode=%s%n", ProcessHandle.current().pid(), nativeMode ? "native" : "monitor");
        blocker.join();
        probe.join();
        System.out.println("两个虚拟线程均已退出");
    }
}
