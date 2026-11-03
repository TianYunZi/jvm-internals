package org.example.jvminternals.ch08;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

/** 正常观察类型身份、反射描述和数组关系，不猜测私有地址或对象偏移。 */
public final class TypeJourney {
    private static final System.Logger LOG = System.getLogger(TypeJourney.class.getName());
    private TypeJourney() { }

    interface Named { String name(); }
    static class Base { int id = 8; }
    static final class Item extends Base implements Named {
        private final String label = "item";
        @Override public String name() { return label; }
    }

    public static void main(String[] args) {
        LOG.log(System.Logger.Level.INFO, "开始类型和反射描述观察");
        Item item = new Item();
        Class<?> type = item.getClass();
        System.out.println("class=" + type.getName() + " mirror.class=" + type.getClass().getName());
        System.out.println("super=" + type.getSuperclass().getName() + " interfaces="
                + Arrays.toString(type.getInterfaces()));
        Field[] fields = type.getDeclaredFields();
        Arrays.sort(fields, Comparator.comparing(Field::getName));
        for (Field field : fields) {
            System.out.println("declared.field=" + field.getName() + " type=" + field.getType().getName());
        }
        Method[] methods = type.getDeclaredMethods();
        Arrays.sort(methods, Comparator.comparing(Method::getName));
        for (Method method : methods) {
            System.out.println("declared.method=" + method.getName() + " returns=" + method.getReturnType().getName());
        }
        System.out.println("item.isBase=" + Base.class.isInstance(item)
                + " item.isNamed=" + Named.class.isInstance(item));
        System.out.println("Object[].from.Item[]=" + Object[].class.isAssignableFrom(Item[].class));
        System.out.println("Named[].from.Item[]=" + Named[].class.isAssignableFrom(Item[].class));
        System.out.println("Object[].from.int[]=" + Object[].class.isAssignableFrom(int[].class));
        System.out.println("Object[].from.int[][]=" + Object[].class.isAssignableFrom(int[][].class));
        System.out.println("int[].isCloneable=" + Cloneable.class.isAssignableFrom(int[].class)
                + " int[].isSerializable=" + Serializable.class.isAssignableFrom(int[].class));
        System.out.println("Item[].component=" + Item[].class.getComponentType().getName()
                + " int[].component=" + int[].class.getComponentType().getName());
        LOG.log(System.Logger.Level.INFO, "类型和反射描述观察结束");
    }
}
