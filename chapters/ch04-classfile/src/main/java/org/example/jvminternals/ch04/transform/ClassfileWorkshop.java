package org.example.jvminternals.ch04.transform;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassTransform;
import java.lang.classfile.Label;
import java.lang.classfile.instruction.ConstantInstruction;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.nio.file.Files;
import java.nio.file.Path;

/** CH04-E04：生成带分支的方法，再用元素流变换替换两个整数常量。 */
public final class ClassfileWorkshop {
    private static final System.Logger LOGGER = System.getLogger(ClassfileWorkshop.class.getName());
    private static final String CLASS_NAME = "GeneratedNumber.class";

    private ClassfileWorkshop() {
    }

    /**
     * 将原始和变换后的产物写入不同子目录；已有文件不覆盖。
     * 程序只生成产物供工具阅读，不加载产物或判断其业务正确性。
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("用法：ClassfileWorkshop <新的输出目录>");
        }
        Path output = Path.of(args[0]);
        Files.createDirectory(output);
        Path originalDirectory = Files.createDirectory(output.resolve("original"));
        Path transformedDirectory = Files.createDirectory(output.resolve("transformed"));
        // 有分支时显式生成栈映射；不要靠手改指令偏移复制旧帧。
        ClassFile classFile = ClassFile.of(ClassFile.StackMapsOption.GENERATE_STACK_MAPS);
        LOGGER.log(System.Logger.Level.INFO, "开始生成具有两个返回分支的类文件");
        byte[] original = classFile.build(
                ClassDesc.of("org.example.jvminternals.ch04.generated.GeneratedNumber"),
                builder -> builder.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL)
                        .withMethodBody("value", MethodTypeDesc.of(ConstantDescs.CD_int, ConstantDescs.CD_boolean),
                                ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC, code -> {
                                    Label negative = code.newLabel();
                                    code.iload(0).ifeq(negative).bipush(7).ireturn()
                                            .labelBinding(negative).bipush(-7).ireturn();
                                }));
        Files.write(originalDirectory.resolve(CLASS_NAME), original);
        byte[] transformed = classFile.transformClass(classFile.parse(original),
                ClassTransform.transformingMethodBodies((builder, element) -> {
                    if (element instanceof ConstantInstruction constant
                            && constant.constantValue() instanceof Integer value) {
                        // 本例生成的方法只包含这两个整数；其他元素原样转交。
                        builder.bipush(value > 0 ? 9 : -9);
                    } else {
                        builder.with(element);
                    }
                }));
        Files.write(transformedDirectory.resolve(CLASS_NAME), transformed);
        System.out.printf("original=%s bytes=%d%n", originalDirectory.resolve(CLASS_NAME), original.length);
        System.out.printf("transformed=%s bytes=%d%n", transformedDirectory.resolve(CLASS_NAME), transformed.length);
        LOGGER.log(System.Logger.Level.INFO, "生成与变换结束，产物未加载执行");
    }
}
