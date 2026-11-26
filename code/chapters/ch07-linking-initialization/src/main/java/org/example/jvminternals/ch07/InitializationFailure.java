package org.example.jvminternals.ch07;

/** 已授权教学故障：同一类首次初始化失败和后续主动使用。 */
public final class InitializationFailure {
    private static final System.Logger LOG = System.getLogger(InitializationFailure.class.getName());
    private InitializationFailure() { }

    static final class Broken {
        static final int VALUE = fail();
        private static int fail() {
            throw new IllegalStateException("教学初始化失败");
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {
        LOG.log(System.Logger.Level.INFO, "开始两次有界失败访问");
        Class<?> discovered = Class.forName(Broken.class.getName(), false,
                InitializationFailure.class.getClassLoader());
        System.out.println("discovered=" + discovered.getName());
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                System.out.println("value=" + Broken.VALUE);
            } catch (ExceptionInInitializerError | NoClassDefFoundError failure) {
                System.out.println("attempt=" + attempt + " error=" + failure);
                Throwable cause = failure.getCause();
                // 原因链最多三层，保留错误传播证据而不无限展开日志。
                for (int depth = 1; cause != null && depth <= 3; depth++) {
                    System.out.println("cause[" + depth + "]=" + cause);
                    cause = cause.getCause();
                }
            }
        }
        LOG.log(System.Logger.Level.INFO, "两次失败访问结束");
    }
}
