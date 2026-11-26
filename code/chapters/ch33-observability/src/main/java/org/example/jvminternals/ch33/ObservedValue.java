package org.example.jvminternals.ch33;

/** 仅改变方法返回常量，类的字段与方法结构保持不变。 */
public final class ObservedValue {
    private ObservedValue() {
    }

    /** 返回值让加载时变换、重转换和重定义的结果可见。 */
    public static String value() {
        return "original";
    }
}
