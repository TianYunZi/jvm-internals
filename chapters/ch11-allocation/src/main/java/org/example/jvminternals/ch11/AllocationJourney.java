package org.example.jvminternals.ch11;

import com.sun.management.ThreadMXBean;
import java.lang.management.ManagementFactory;

/** 比较逻辑创建的不同物理表示，仅记录分配字节，不测运行性能。 */
public final class AllocationJourney {
    private static volatile Pair publishedPair;

    private AllocationJourney() {
    }

    /** 固定调用次数，保留阶段结果，给外部编译与分配记录提供边界。 */
    public static void main(String[] args) {
        ThreadMXBean meter = ManagementFactory.getPlatformMXBean(ThreadMXBean.class);
        if (meter == null || !meter.isThreadAllocatedMemorySupported()) {
            System.out.println("本环境不支持线程分配字节，结束观察");
            return;
        }
        if (!meter.isThreadAllocatedMemoryEnabled()) {
            meter.setThreadAllocatedMemoryEnabled(true);
        }
        long sum = 0;
        for (int i = 0; i < 20_000; i++) {
            sum += localSum(i) + escapedSum(i);
        }
        System.out.println("阶段=预备结束；结果=" + sum);
        observeLocal(meter);
        observeEscaped(meter);
        // 末尾读取发布对象，明确存在可观察的堆对象持有者。
        System.out.println("阶段=保留对象；值=" + publishedPair.left);
        publishedPair = null;
        System.gc();
        System.out.println("阶段=结束；回收仅为请求");
    }

    private static void observeLocal(ThreadMXBean meter) {
        System.out.println("阶段=局部对象开始；次数=200000");
        long before = meter.getCurrentThreadAllocatedBytes();
        long sum = 0;
        for (int i = 0; i < 200_000; i++) {
            sum += localSum(i);
        }
        long bytes = meter.getCurrentThreadAllocatedBytes() - before;
        System.out.println("阶段=局部对象结束；结果=" + sum + "；近似分配字节=" + bytes);
    }

    private static void observeEscaped(ThreadMXBean meter) {
        System.out.println("阶段=保留对象开始；次数=200000");
        long before = meter.getCurrentThreadAllocatedBytes();
        long sum = 0;
        for (int i = 0; i < 200_000; i++) {
            sum += escapedSum(i);
        }
        long bytes = meter.getCurrentThreadAllocatedBytes() - before;
        System.out.println("阶段=保留对象结束；结果=" + sum + "；近似分配字节=" + bytes);
    }

    // 只取字段计算结果，编译器有机会消除分配；命令显式保留独立编译边界。
    private static int localSum(int input) {
        Pair pair = new Pair(input, input + 1);
        return pair.left + pair.right;
    }

    // volatile 发布让对象逃出当前调用，但不意味着对象之后永远不可回收。
    private static int escapedSum(int input) {
        Pair pair = new Pair(input, input + 1);
        publishedPair = pair;
        return pair.left + pair.right;
    }

    private static final class Pair {
        private final int left;
        private final int right;
        private Pair(int left, int right) {
            this.left = left;
            this.right = right;
        }
    }
}
