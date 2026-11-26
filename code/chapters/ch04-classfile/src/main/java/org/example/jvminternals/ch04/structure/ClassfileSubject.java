package org.example.jvminternals.ch04.structure;

/**
 * CH04-E01：供反汇编阅读的泛型类；不承担校验或故障复现职责。
 * 泛型上界令字段描述符与泛型签名呈现不同的信息。
 *
 * @param <T> 数值类型
 */
public final class ClassfileSubject<T extends Number> {
    public static final String LABEL = "classfile";
    private final T value;

    /** 保存数值，供泛型字段与访问方法的结构观察使用。 */
    public ClassfileSubject(T value) {
        this.value = value;
    }

    /** 返回数值；描述符中的擦除类型为 Number。 */
    public T value() {
        return value;
    }

    /**
     * 把输入转为整数，空输入或非整数输入采用字段值。
     * 分支和异常处理用于展示 Code、异常表及栈映射之间的关系。
     */
    public int parseOrDefault(String input) {
        if (input == null) {
            return value.intValue();
        }
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException exception) {
            // 输入不符合整数格式属于本方法的正常回退契约。
            return value.intValue();
        }
    }
}
