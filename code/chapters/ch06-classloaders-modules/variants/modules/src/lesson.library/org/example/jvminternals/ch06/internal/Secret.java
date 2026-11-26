package org.example.jvminternals.ch06.internal;

/** 类本身是 public，包是否导出或开放仍由模块规则决定。 */
public final class Secret {
    private Secret() {
    }

    /** 用于普通跨模块访问检查的公开方法。 */
    public static String message() {
        return "内部包公开成员调用成功";
    }

    private static String hidden() {
        return "深反射读取成功";
    }
}
