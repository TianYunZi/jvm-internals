package org.example.jvminternals.ch10;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.lang.management.ManagementFactory;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.vm.VM;

/** 展示当前进程中的布局，不把样本偏移当作跨版本固定契约。 */
public final class LayoutJourney {
    private LayoutJourney() {
    }

    /** 固定有限样本，输出有效参数、布局与共享对象图后立即退出。 */
    public static void main(String[] args) {
        HotSpotDiagnosticMXBean diagnostic = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
        System.out.println("阶段=配置；工具=JOL 0.18-SNAPSHOT db2cc744");
        for (String name : new String[]{"UseCompactObjectHeaders", "UseCompressedOops",
                "UseCompressedClassPointers", "ObjectAlignmentInBytes", "LockingMode"}) {
            System.out.println(name + "=" + diagnostic.getVMOption(name).getValue());
        }
        System.out.println(VM.current().details());
        byte[] shared = new byte[10];
        show("空对象", new Object());
        show("一个整数", new SingleInt(7));
        show("混合字段", new Mixed(7, (byte) 1, shared));
        show("继承对象", new Child(11L, (byte) 2, 13, shared));
        show("空字节数组", new byte[0]);
        show("一字节数组", new byte[1]);
        show("整数数组", new int[3]);
        show("长整数数组", new long[1]);
        show("引用数组", new Object[3]);
        // 两个包装对象共用内容，图大小只对可达实例计一次。
        Mixed left = new Mixed(1, (byte) 1, shared);
        Mixed right = new Mixed(2, (byte) 2, shared);
        System.out.println("阶段=共享图；对象数=" + GraphLayout.parseInstance(left, right).totalCount()
                + "；总字节=" + GraphLayout.parseInstance(left, right).totalSize());
        // 哈希是运行时状态，大小通常不随这次安装哈希而改变。
        Object identity = new Object();
        System.out.println("阶段=哈希前\n" + ClassLayout.parseInstance(identity).toPrintable());
        System.out.println("身份哈希=" + System.identityHashCode(identity));
        System.out.println("阶段=哈希后\n" + ClassLayout.parseInstance(identity).toPrintable());
        System.out.println("阶段=结束");
    }

    private static void show(String label, Object value) {
        System.out.println("样本=" + label + "\n" + ClassLayout.parseInstance(value).toPrintable());
    }

    // 字段由布局工具反射读取；排列刻意混合，以观察实际重排与空洞。
    private static final class SingleInt {
        private final int number;
        private SingleInt(int number) {
            this.number = number;
        }
    }

    private static final class Mixed {
        private final int number;
        private final byte tag;
        private final Object reference;
        private Mixed(int number, byte tag, Object reference) {
            this.number = number;
            this.tag = tag;
            this.reference = reference;
        }
    }

    private static class Base {
        private final long sequence;
        private final byte tag;
        private Base(long sequence, byte tag) {
            this.sequence = sequence;
            this.tag = tag;
        }
    }

    private static final class Child extends Base {
        private final int count;
        private final Object reference;
        private Child(long sequence, byte tag, int count, Object reference) {
            super(sequence, tag);
            this.count = count;
            this.reference = reference;
        }
    }
}
