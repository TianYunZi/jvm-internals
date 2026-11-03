package org.example.jvminternals.ch06.provider;

import org.example.jvminternals.ch06.Greeter;

/** 外部服务提供者，其 Greeter 类型由父加载器提供。 */
public final class FriendlyGreeter implements Greeter {
    @Override
    public String greet() {
        return "来自隔离提供者的问候";
    }
}
