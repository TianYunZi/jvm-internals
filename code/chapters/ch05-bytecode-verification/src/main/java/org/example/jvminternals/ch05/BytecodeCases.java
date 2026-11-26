package org.example.jvminternals.ch05;

/** CH05-E01：供逐指令阅读的合法方法组，普通执行不触发错误输入。 */
public final class BytecodeCases {
    private static final System.Logger LOGGER = System.getLogger(BytecodeCases.class.getName());
    private final MonitorCell monitor = new MonitorCell();

    /** 三个参数占用五个槽，long 临时值另占两个槽。 */
    public static long widenAndAdd(int count, long base, double extra) {
        long partial = count + base;
        return partial + (long) extra;
    }

    /** 两个词法作用域的局部变量可在类文件中使用同一槽。 */
    public static int reuseSlot(int value) {
        int result;
        {
            int first = value + 1;
            result = first;
        }
        {
            int second = result * 2;
            return second;
        }
    }

    /** 两条分支在同一个 int 局部变量处汇合。 */
    public static int branch(int value) {
        int result;
        if (value >= 0) {
            result = value + 1;
        } else {
            result = -value;
        }
        return result * 2;
    }

    /** 数组循环用于观察索引、边界检查语义及回边。 */
    public static int sum(int[] values) {
        int result = 0;
        for (int index = 0; index < values.length; index++) {
            result += values[index];
        }
        return result;
    }

    /** 紧密排列的分支键，实际指令形态以本轮反汇编为准。 */
    public static int denseSwitch(int value) {
        return switch (value) {
            case 0 -> 10;
            case 1 -> 20;
            case 2 -> 30;
            case 3 -> 40;
            default -> -1;
        };
    }

    /** 稀疏分支键，供比较查找式选择指令。 */
    public static int sparseSwitch(int value) {
        return switch (value) {
            case -1000 -> 10;
            case 1 -> 20;
            case 10000 -> 30;
            default -> -1;
        };
    }

    /** 块同步和方法同步都使用同一个稳定私有监视器，避免混用保护规则。 */
    public int synchronizedBlock(int delta) {
        synchronized (monitor) {
            monitor.value += delta;
            return monitor.value;
        }
    }

    /** 通过私有对象的同步方法展示方法访问标志。 */
    public int synchronizedMethod(int delta) {
        return monitor.add(delta);
    }

    /** 对接口类型发起调用，正常接收者由入口固定构造。 */
    private static int invoke(Unary operation, int value) {
        return operation.apply(value);
    }

    /** 输出少量教学结果，不比较预期值、不包含断言或压力循环。 */
    public static void main(String[] args) {
        LOGGER.log(System.Logger.Level.INFO, "开始执行字节码教学方法组");
        BytecodeCases cases = new BytecodeCases();
        System.out.println("widen=" + widenAndAdd(2, 10L, 3.5));
        System.out.println("reuse=" + reuseSlot(3));
        System.out.println("branch=" + branch(3));
        System.out.println("sum=" + sum(new int[]{2, 4, 6}));
        System.out.println("dense=" + denseSwitch(2) + " sparse=" + sparseSwitch(10000));
        System.out.println("block=" + cases.synchronizedBlock(2));
        System.out.println("method=" + cases.synchronizedMethod(3));
        System.out.println("interface=" + invoke(new Increment(), 7));
        LOGGER.log(System.Logger.Level.INFO, "字节码教学方法组结束");
    }

    private interface Unary {
        int apply(int value);
    }

    private static final class Increment implements Unary {
        @Override
        public int apply(int value) {
            return value + 1;
        }
    }

    /** 对象不向外暴露，块和同步方法均锁定该同一对象。 */
    private static final class MonitorCell {
        private int value;

        private synchronized int add(int delta) {
            value += delta;
            return value;
        }
    }
}
