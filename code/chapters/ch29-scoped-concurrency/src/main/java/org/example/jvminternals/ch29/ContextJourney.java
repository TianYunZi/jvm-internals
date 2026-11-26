package org.example.jvminternals.ch29;

/** 普通编译展示线程局部清理与正式作用域值，不依赖预览接口。 */
public final class ContextJourney {
    private static final ThreadLocal<String> LOCAL = new ThreadLocal<>();
    private static final ScopedValue<String> REQUEST = ScopedValue.newInstance();

    private ContextJourney() { }

    /** 嵌套绑定恢复，线程局部值由拥有者显式移除。 */
    public static void main(String[] args) {
        LOCAL.set("legacy-context");
        try {
            System.out.println("线程局部：" + LOCAL.get());
            ScopedValue.where(REQUEST, "outer").run(() -> {
                show("外层");
                ScopedValue.where(REQUEST, "inner").run(() -> show("内层"));
                show("内层结束");
            });
            System.out.println("作用域退出：isBound=" + REQUEST.isBound());
        } finally {
            LOCAL.remove();
            System.out.println("线程局部已remove");
        }
    }

    private static void show(String phase) {
        System.out.println(phase + "：" + REQUEST.get());
    }
}
