package org.example.jvminternals.ch33;

import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;
import java.time.Instant;

/** 三个变换阶段后保留十二秒窗口，供同一目标的诊断观察。 */
public final class ObservationJourney {
    private ObservationJourney() {
    }

    /** 有界普通进程，代理必须由启动命令显式指定。 */
    public static void main(String[] args)
            throws IOException, UnmodifiableClassException, ClassNotFoundException, InterruptedException {
        System.out.printf("目标 pid=%d time=%s value=%s%n",
                ProcessHandle.current().pid(), Instant.now(), ObservedValue.value());
        ObservationAgent.prepare("retransform").retransformClasses(ObservedValue.class);
        System.out.println("重转换 value=" + ObservedValue.value());
        try (var input = ObservedValue.class.getResourceAsStream("ObservedValue.class")) {
            if (input == null) {
                throw new IOException("未找到正式类文件资源");
            }
            ObservationAgent.prepare("redefine").redefineClasses(
                    new ClassDefinition(ObservedValue.class, input.readAllBytes()));
        }
        System.out.println("重定义 value=" + ObservedValue.value());
        Thread virtual = Thread.ofVirtual().name("observed-virtual").start(() -> {
            try {
                Thread.sleep(12_000);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                System.err.println("观察窗口被中断，虚拟线程收尾");
            }
        });
        System.out.println("诊断窗口开始");
        virtual.join();
        System.out.println("诊断窗口结束");
    }
}
