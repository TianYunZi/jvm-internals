package org.example.jvminternals.ch32;

/** 以短本地调用显示引用、临界区和外部线程的责任边界。 */
public final class NativeJourney {
    private NativeJourney() {
    }

    private static native boolean acquire(Object value);
    private static native long sumCritical(int[] values);
    private static native int attachedCallback();
    private static native void release();

    /** 由已附加的原生线程调用；不能把调用者线程的JNIEnv转交给它。 */
    public static void onNativeThread() {
        System.out.printf("CH32 原生线程回调：%s virtual=%s%n",
                Thread.currentThread().getName(), Thread.currentThread().isVirtual());
    }

    /** 强全局引用在finally显式释放；弱引用不承担保持对象存活的责任。 */
    public static void main(String[] args) {
        System.load(System.getProperty("ch32.library"));
        System.out.println("CH32 本地库已加载");
        try {
            boolean held = acquire(new byte[16]);
            System.out.printf("CH32 跨调用引用已建立=%s%n", held);
            if (held) {
                System.out.printf("CH32 临界区求和=%d%n", sumCritical(new int[]{2, 3, 5}));
                System.out.printf("CH32 附加线程返回状态=%d%n", attachedCallback());
            }
        } finally {
            release();
            System.out.println("CH32 全局及弱全局引用已释放");
        }
    }
}
