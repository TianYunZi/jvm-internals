package org.example.jvminternals.ch30;

/** JDK 25正式实例入口；带参数候选优先于无参数候选。 */
public final class InstanceEntry {
    InstanceEntry() {
        System.out.println("CH30 实例入口：先执行无参数构造器");
    }

    void main(String[] args) {
        System.out.printf("CH30 选择带参数实例main：参数数=%d%n", args.length);
    }

    void main() {
        System.out.println("CH30 无参数候选main");
    }
}
