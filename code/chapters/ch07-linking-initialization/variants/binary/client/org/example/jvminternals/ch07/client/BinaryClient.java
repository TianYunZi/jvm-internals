package org.example.jvminternals.ch07.client;

import org.example.jvminternals.ch07.library.Api;

/** 只编译一次；运行时分别搭配隔离库，不重新适配源文件。 */
public final class BinaryClient {
    private BinaryClient() { }
    public static void main(String[] args) {
        System.out.println(Api.greet());
    }
}
