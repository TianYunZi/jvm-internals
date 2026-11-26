package org.example.jvminternals.ch07;

/** 常量、运行期赋值、相关接口、递归和实际接收者的正常教学观察。 */
public final class InitializationJourney {
    private static final System.Logger LOG = System.getLogger(InitializationJourney.class.getName());

    private InitializationJourney() { }

    static int mark(String phase, int value) {
        System.out.println("initialize=" + phase + " value=" + value);
        return value;
    }

    public static final class Values {
        public static final int CONSTANT = 41;
        public static final int RUNTIME = mark("Values.RUNTIME", 42);
        private Values() { }
    }

    interface HasDefault {
        int TOKEN = mark("HasDefault", 20);
        default int token() { return TOKEN; }
    }

    interface Plain {
        int TOKEN = mark("Plain", 30);
    }

    static class Parent {
        static final int VALUE = mark("Parent", 10);
    }

    static final class Child extends Parent implements HasDefault, Plain {
        static final int VALUE = mark("Child", 40);
    }

    static final class Recursive {
        static final int FIRST = readLater();
        static int later = 7;

        private static int readLater() {
            // 同一初始化线程递归调用，尚未执行的 later 赋值仍不可凭空出现。
            return later + 1;
        }
    }

    public static final class Lazy {
        public static int value = mark("Lazy", 50);
        private Lazy() { }
    }

    static class Base {
        int field = 1;
        int number() { return 1; }
    }

    static final class Derived extends Base {
        int field = 2;
        @Override int number() { return 2; }
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        LOG.log(System.Logger.Level.INFO, "开始正常初始化观察");
        System.out.println("constant=" + Values.CONSTANT);
        System.out.println("before.runtime");
        System.out.println("runtime=" + Values.RUNTIME);
        System.out.println("child=" + Child.VALUE);
        System.out.println("plain=" + Plain.TOKEN);
        System.out.println("recursive.first=" + Recursive.FIRST + " later=" + Recursive.later);
        Class<?> discovered = Class.forName(Lazy.class.getName(), false,
                InitializationJourney.class.getClassLoader());
        System.out.println("discovered=" + discovered.getName());
        System.out.println("reflect.value=" + discovered.getField("value").get(null));
        Base receiver = new Derived();
        System.out.println("field=" + receiver.field + " virtual=" + receiver.number());
        LOG.log(System.Logger.Level.INFO, "正常初始化观察结束");
    }
}
