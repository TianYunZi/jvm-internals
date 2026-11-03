package org.example.jvminternals.ch07.library;

/** 已授权不兼容变体：名称和描述符保留，访问权限收紧。 */
public final class Api {
    private Api() { }
    private static String greet() { return "private"; }
}
