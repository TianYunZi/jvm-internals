package org.example.jvminternals.ch04.modern;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.util.Arrays;

/** CH04-E02：将现代类声明、嵌套关系与反射可见信息放在同一组产物中观察。 */
public final class ModernShapes {
    private static final System.Logger LOGGER = System.getLogger(ModernShapes.class.getName());
    private final int offset;

    /** 创建外部实例；非静态成员类会保留对该实例的引用。 */
    public ModernShapes(int offset) {
        this.offset = offset;
    }

    /** 运行期保留的类型注解，供反射读取。 */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface RuntimeTag {
    }

    /** 只保留在类文件中的类型注解，普通运行期注解查询不可见。 */
    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.TYPE)
    public @interface ClassTag {
    }

    /** 密封接口的允许实现集合写入 PermittedSubclasses 属性。 */
    public sealed interface Shape permits Point {
    }

    /** 记录组件由 Record 属性描述，访问器等仍以普通方法表示。 */
    @RuntimeTag
    @ClassTag
    public record Point(int x, int y) implements Shape {
    }

    /** 非静态成员类访问外部私有字段，用于解释内部类和巢成员的不同职责。 */
    public final class Member {
        /** 返回外部实例字段；它与巢主关系及外部实例引用分别有关。 */
        public int offset() {
            return ModernShapes.this.offset;
        }
    }

    /** 输出反射元数据；不比较预期值、不包含断言。 */
    public static void main(String[] args) {
        LOGGER.log(System.Logger.Level.INFO, "开始读取记录类、密封接口与注解元数据");
        System.out.println("Point.isRecord=" + Point.class.isRecord());
        System.out.println("Point.recordComponents=" + Arrays.toString(Point.class.getRecordComponents()));
        System.out.println("Point.annotations=" + Arrays.toString(Point.class.getDeclaredAnnotations()));
        System.out.println("Shape.isSealed=" + Shape.class.isSealed());
        System.out.println("Shape.permitted=" + Arrays.toString(Shape.class.getPermittedSubclasses()));
        System.out.println("Member.nestHost=" + Member.class.getNestHost().getName());
        LOGGER.log(System.Logger.Level.INFO, "元数据读取结束");
    }
}
