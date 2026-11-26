package org.example.jvminternals.ch11;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

/** 只读观察字符串包装、内容数组、驻留和异步去重，不修改内部存储。 */
public final class StringJourney {
    private StringJourney() {
    }

    /** 反射需显式开放 java.lang；所有观察与回收请求都有固定上限。 */
    public static void main(String[] args) throws ReflectiveOperationException, InterruptedException {
        Field value = String.class.getDeclaredField("value");
        Field coder = String.class.getDeclaredField("coder");
        value.setAccessible(true);
        coder.setAccessible(true);
        String first = new String(new char[]{'m', 'e', 'm', 'o'});
        String copied = new String(first);
        String separate = new String(new char[]{'m', 'e', 'm', 'o'});
        System.out.println("阶段=表示；值相等=" + first.equals(separate)
                + "；原对象相同=" + (first == separate)
                + "；复制构造共享内容=" + (value.get(first) == value.get(copied))
                + "；独立构造共享内容=" + (value.get(first) == value.get(separate)));
        System.out.println("阶段=驻留；规范对象相同=" + (first.intern() == separate.intern())
                + "；原对象仍不同=" + (first != separate));
        System.out.println("阶段=编码；拉丁coder=" + coder.getByte(first)
                + "；汉字coder=" + coder.getByte("内存"));
        String[] retained = new String[4_000];
        char[] content = new char[128];
        Arrays.fill(content, 'x');
        // 每次复制字符输入以得到独立内容数组，避免把复制构造共享误认成去重。
        for (int i = 0; i < retained.length; i++) {
            retained[i] = new String(content);
        }
        report("持有", retained, value);
        for (int round = 1; round <= 2; round++) {
            System.gc();
            Thread.sleep(500);
            report("请求后" + round, retained, value);
        }
        System.out.println("阶段=结束；保留字符串=" + retained.length + "；示例长度=" + retained[0].length());
    }

    private static void report(String phase, String[] retained, Field value) throws IllegalAccessException {
        Map<Object, Boolean> payloads = new IdentityHashMap<>();
        for (String text : retained) {
            payloads.put(value.get(text), Boolean.TRUE);
        }
        System.out.println("阶段=" + phase + "；内容数组身份数=" + payloads.size());
    }
}
