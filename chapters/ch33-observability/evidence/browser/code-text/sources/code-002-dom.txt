package org.example.jvminternals.ch33;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/** 启动时显式加载，避免把动态附加许可和类变换能力混为一谈。 */
public final class ObservationAgent implements ClassFileTransformer {
    private static Instrumentation instrumentation;
    private static volatile String replacement = "load";

    /** 注册允许重转换的代理；清单同时声明重定义能力。 */
    public static void premain(String options, Instrumentation value) {
        instrumentation = value;
        value.addTransformer(new ObservationAgent(), true);
        System.out.printf("代理启动 retransform=%s redefine=%s%n",
                value.isRetransformClassesSupported(), value.isRedefineClassesSupported());
    }

    /** 后续重转换仍从虚拟机提供的输入出发，不叠加上次输出。 */
    public static Instrumentation prepare(String next) {
        replacement = next;
        return instrumentation;
    }

    /** 只处理一个固定教学类；其他类明确保持原样。 */
    @Override
    public byte[] transform(Module module, ClassLoader loader, String name, Class<?> redefined,
                            ProtectionDomain domain, byte[] bytes) {
        if (!"org/example/jvminternals/ch33/ObservedValue".equals(name)) {
            return null;
        }
        String current = replacement;
        System.out.printf("变换回调 phase=%s inputBytes=%d replacement=%s%n",
                redefined == null ? "load" : "existing", bytes.length, current);
        ClassFile classFile = ClassFile.of();
        return classFile.transformClass(classFile.parse(bytes),
                ClassTransform.transformingMethodBodies((builder, element) -> {
                    if (element instanceof ConstantInstruction constant
                            && "original".equals(constant.constantValue())) {
                        builder.ldc(current);
                    } else {
                        builder.with(element);
                    }
                }));
    }
}
