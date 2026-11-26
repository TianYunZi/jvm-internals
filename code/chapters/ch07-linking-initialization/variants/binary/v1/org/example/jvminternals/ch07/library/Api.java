package org.example.jvminternals.ch07.library;

/** 客户端编译时看到的公开静态方法。 */
public final class Api {
    private Api() { }
    public static String greet() { return "library-v1"; }
}
