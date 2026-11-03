package org.example.jvminternals.ch06.isolated;

/** 同一份字节由不同定义加载器加载，签名中的 Plugin 随各自命名空间解析。 */
public final class Plugin {
    /** 返回本加载器命名空间中的同类型对象。 */
    public Plugin echo(Plugin value) {
        return value;
    }
}
