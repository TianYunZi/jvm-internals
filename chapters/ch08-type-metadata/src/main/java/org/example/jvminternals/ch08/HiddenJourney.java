package org.example.jvminternals.ch08;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;

/** 生成一份合法类文件，再用当前同包 Lookup 定义为非 STRONG 隐藏类。 */
public final class HiddenJourney {
    private static final System.Logger LOG = System.getLogger(HiddenJourney.class.getName());
    private HiddenJourney() { }

    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        if (args.length != 1) {
            throw new IllegalArgumentException("用法：HiddenJourney <生成类文件输出路径>");
        }
        LOG.log(System.Logger.Level.INFO, "开始生成并定义一个隐藏类");
        byte[] bytes = ClassFile.of().build(ClassDesc.of("org.example.jvminternals.ch08.HiddenValue"),
                builder -> builder.withFlags(ClassFile.ACC_PUBLIC | ClassFile.ACC_FINAL)
                        .withMethodBody("value", MethodTypeDesc.of(ConstantDescs.CD_int),
                                ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                                code -> code.bipush(42).ireturn()));
        Path output = Path.of(args[0]);
        Files.createDirectories(output.toAbsolutePath().getParent());
        Files.write(output, bytes);
        // 保存的是定义前字节；运行时隐藏名称由虚拟机产生，不写回原文件。
        Class<?> hidden = MethodHandles.lookup().defineHiddenClass(bytes, true,
                MethodHandles.Lookup.ClassOption.NESTMATE).lookupClass();
        System.out.println("name=" + hidden.getName() + " hidden=" + hidden.isHidden());
        System.out.println("loader=" + hidden.getClassLoader().getName()
                + " nest.host=" + hidden.getNestHost().getName());
        System.out.println("nominal.descriptor.present=" + hidden.describeConstable().isPresent());
        System.out.println("value=" + hidden.getDeclaredMethod("value").invoke(null));
        LOG.log(System.Logger.Level.INFO, "隐藏类观察结束");
    }
}
