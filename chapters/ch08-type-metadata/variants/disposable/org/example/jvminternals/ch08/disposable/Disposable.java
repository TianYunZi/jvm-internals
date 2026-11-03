package org.example.jvminternals.ch08.disposable;

/** 由短期加载器独立定义；不放入主输出类路径。 */
public final class Disposable {
    private final int value = 8;
    public int value() { return value; }
}
