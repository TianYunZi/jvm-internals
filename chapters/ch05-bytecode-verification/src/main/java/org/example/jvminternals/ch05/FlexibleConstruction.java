package org.example.jvminternals.ch05;

/** CH05-E02：正常构造案例，展示前置计算、当前类字段赋值及父类构造调用。 */
public final class FlexibleConstruction {
    private static final System.Logger LOGGER = System.getLogger(FlexibleConstruction.class.getName());

    private FlexibleConstruction() {
    }

    /** 只创建正常对象并读取完成构造后的值。 */
    public static void main(String[] args) {
        LOGGER.log(System.Logger.Level.INFO, "开始执行灵活构造器体示例");
        Child child = new Child(7);
        System.out.printf("original=%d normalized=%d%n", child.original, child.normalized());
        LOGGER.log(System.Logger.Level.INFO, "灵活构造器体示例结束");
    }

    private static class Base {
        private final int normalized;

        private Base(int normalized) {
            this.normalized = normalized;
        }

        final int normalized() {
            return normalized;
        }
    }

    private static final class Child extends Base {
        private final int original;

        private Child(int value) {
            int normalized = Math.max(value, 0);
            // 当前类字段的前置赋值不等于可以任意读取或泄漏未构造完成的 this。
            this.original = value;
            super(normalized);
        }
    }
}
