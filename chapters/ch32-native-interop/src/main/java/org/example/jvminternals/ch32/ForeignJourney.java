package org.example.jvminternals.ch32;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;

/** 用同步回调使库、段、桩与Arena的有效期处于同一个词法范围。 */
public final class ForeignJourney {
    private ForeignJourney() {
    }

    /** 本地调用约定不允许Java异常任意跨越回调边界；本回调只做有限整数运算。 */
    public static int adjust(int value) {
        return value + 10;
    }

    /** MethodHandle精确调用声明Throwable；异常交给进程入口，不吞掉失败上下文。 */
    public static void main(String[] args) throws Throwable {
        MemorySegment heap = MemorySegment.ofArray(new int[]{2, 3, 5});
        System.out.printf("CH32 堆内段 bytes=%d native=%s%n", heap.byteSize(), heap.isNative());
        MemorySegment.Scope scope;
        try (Arena arena = Arena.ofConfined()) {
            scope = arena.scope();
            MemorySegment data = arena.allocateFrom(ValueLayout.JAVA_INT, 2, 3, 5);
            System.out.printf("CH32 本地段 bytes=%d alive=%s owner=%s first=%d%n",
                    data.byteSize(), scope.isAlive(), data.isAccessibleBy(Thread.currentThread()),
                    data.get(ValueLayout.JAVA_INT, 0));
            Linker linker = Linker.nativeLinker();
            SymbolLookup symbols = SymbolLookup.libraryLookup(
                    Path.of(System.getProperty("ch32.library")), arena);
            FunctionDescriptor callbackType = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
            MethodHandle callback = MethodHandles.lookup().findStatic(
                    ForeignJourney.class, "adjust", callbackType.toMethodType());
            MemorySegment stub = linker.upcallStub(callback, callbackType, arena);
            MethodHandle transform = linker.downcallHandle(symbols.findOrThrow("ch32_transform"),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
            int result = (int) transform.invokeExact(7, stub);
            System.out.printf("CH32 下行调用及同步上行回调 result=%d%n", result);
            // 本地函数不保留段或回调指针；返回后才允许关闭所属Arena。
        }
        System.out.printf("CH32 Arena关闭后 alive=%s；不再解引用本地段或调用桩%n", scope.isAlive());
    }
}
