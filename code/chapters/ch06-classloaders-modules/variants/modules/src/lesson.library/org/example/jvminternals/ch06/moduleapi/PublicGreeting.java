package org.example.jvminternals.ch06.moduleapi;

/** 命名模块中正常可访问的公开入口。 */
public final class PublicGreeting {
    private PublicGreeting() {
    }

    /** 返回本模块的公开信息。 */
    public static String message() {
        return "公开包调用成功";
    }
}
