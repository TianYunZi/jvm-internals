package org.example.jvminternals.ch06;

/** CH06-E02：由父加载器共享的服务类型，提供者在隔离输出中实现。 */
public interface Greeter {
    /** 返回一条短教学问候。 */
    String greet();
}
