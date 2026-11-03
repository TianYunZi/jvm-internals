package org.example.jvminternals.ch04.model;

import java.io.IOException;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.nio.file.Path;

/** CH04-E03：读取指定类文件，按字段、方法和属性层级输出不可变模型。 */
public final class ClassfileExplorer {
    private static final System.Logger LOGGER = System.getLogger(ClassfileExplorer.class.getName());

    private ClassfileExplorer() {
    }

    /**
     * 接收一个本地类文件路径；输入错误由进程入口传播并显示原因。
     * 只访问模型，不定义类、不运行目标方法，也不调用校验器。
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("用法：ClassfileExplorer <类文件路径>");
        }
        LOGGER.log(System.Logger.Level.INFO, "开始读取类文件模型");
        ClassModel model = ClassFile.of().parse(Path.of(args[0]));
        System.out.printf("class=%s version=%d.%d%n", model.thisClass().asInternalName(),
                model.majorVersion(), model.minorVersion());
        for (var field : model.fields()) {
            System.out.printf("field=%s descriptor=%s%n", field.fieldName().stringValue(),
                    field.fieldType().stringValue());
        }
        for (var method : model.methods()) {
            System.out.printf("method=%s descriptor=%s%n", method.methodName().stringValue(),
                    method.methodType().stringValue());
            method.code().ifPresent(code -> {
                for (var element : code) {
                    System.out.printf("  code-element=%s%n", element);
                }
            });
        }
        for (var attribute : model.attributes()) {
            System.out.println("class-attribute=" + attribute.attributeName().stringValue());
        }
        LOGGER.log(System.Logger.Level.INFO, "类文件模型读取结束");
    }
}
