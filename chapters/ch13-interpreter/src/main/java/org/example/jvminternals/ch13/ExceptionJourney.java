package org.example.jvminternals.ch13;

/** 用已捕获的异常连接字节码异常表、创建时栈和逐层展开。 */
public final class ExceptionJourney {
    private ExceptionJourney() {
    }

    /** 两种语言机制均在main中完成处理，正常结束进程。 */
    public static void main(String[] args) {
        System.out.println("阶段=解释模式异常观察开始");
        for (int mode = 0; mode < 2; mode++) {
            try {
                middle(mode);
            } catch (IllegalArgumentException | ArithmeticException failure) {
                System.out.println("阶段=外层捕获；类型=" + failure.getClass().getSimpleName());
                // 输出Throwable创建时的栈，不把它误称为捕获时仍活动的物理帧。
                for (StackTraceElement frame : failure.getStackTrace()) {
                    if (frame.getClassName().equals(ExceptionJourney.class.getName())) {
                        System.out.println("创建时栈=" + frame);
                    }
                }
            }
        }
        System.out.println("阶段=正常结束");
    }

    private static void middle(int mode) {
        try {
            leaf(mode);
        } finally {
            // finally由编译器展开；其异常表也是本章的观察对象。
            System.out.println("阶段=中层finally；模式=" + mode);
        }
    }

    private static void leaf(int mode) {
        if (mode == 0) {
            throw new IllegalArgumentException("教学显式抛出");
        }
        int divisor = mode - 1;
        System.out.println("商=" + 12 / divisor);
    }
}
