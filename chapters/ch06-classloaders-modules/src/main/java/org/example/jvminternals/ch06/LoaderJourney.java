package org.example.jvminternals.ch06;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** CH06-E01：默认委派下的单名定义，展示同名类型、签名和已授权转换失败。 */
public final class LoaderJourney {
    private static final System.Logger LOGGER = System.getLogger(LoaderJourney.class.getName());
    private static final String PLUGIN = "org.example.jvminternals.ch06.isolated.Plugin";

    private LoaderJourney() {
    }

    /** 接收本章 Plugin 类文件，所有输入均为本地已编译教学产物。 */
    public static void main(String[] args) throws IOException, ReflectiveOperationException {
        if (args.length != 1) {
            throw new IllegalArgumentException("用法：LoaderJourney <Plugin 类文件>");
        }
        LOGGER.log(System.Logger.Level.INFO, "开始读取加载器身份并定义两份同名类型");
        System.out.println("Object.loader=" + loaderName(Object.class.getClassLoader()));
        Class<?> sqlDriver = Class.forName("java.sql.Driver");
        System.out.println("Driver.loader=" + loaderName(sqlDriver.getClassLoader()));
        System.out.println("Driver.module=" + sqlDriver.getModule().getName());
        System.out.println("application.loader=" + loaderName(LoaderJourney.class.getClassLoader()));
        System.out.println("object.resource=" + Object.class.getResource("/java/lang/Object.class"));
        System.out.println("application.resource=" + LoaderJourney.class.getClassLoader().getResource("notes.txt"));
        SingleNameLoader left = new SingleNameLoader("left", Path.of(args[0]));
        SingleNameLoader right = new SingleNameLoader("right", Path.of(args[0]));
        Class<?> leftType = Class.forName(PLUGIN, false, left);
        Class<?> rightType = Class.forName(PLUGIN, false, right);
        Object leftObject = leftType.getConstructor().newInstance();
        Object rightObject = rightType.getConstructor().newInstance();
        System.out.println("same.binaryName=" + leftType.getName().equals(rightType.getName()));
        System.out.println("same.type=" + (leftType == rightType));
        System.out.println("left.echo.parameter.loader=" + loaderName(
                leftType.getMethod("echo", leftType).getParameterTypes()[0].getClassLoader()));
        System.out.println("right.echo.parameter.loader=" + loaderName(
                rightType.getMethod("echo", rightType).getParameterTypes()[0].getClassLoader()));
        System.out.println("left.echo.result.loader=" + loaderName(leftType.getMethod("echo", leftType)
                .invoke(leftObject, leftObject).getClass().getClassLoader()));
        try {
            leftType.cast(rightObject);
        } catch (ClassCastException exception) {
            // 用户已明确授权的隔离转换失败；记录异常数据，不添加断言。
            System.out.println("cross.cast=" + exception.getClass().getSimpleName() + ": " + exception.getMessage());
        }
        ClassLoader relay = new ClassLoader("relay", left) {
            // 只使用继承的默认委派，不另行定义 Plugin。
        };
        Class<?> requested = Class.forName(PLUGIN, false, relay);
        System.out.println("request.loader=" + relay.getName());
        System.out.println("request.result.definer=" + loaderName(requested.getClassLoader()));
        System.out.println("relay.same.as.left=" + (requested == leftType));
        LOGGER.log(System.Logger.Level.INFO, "加载器身份观察结束");
    }

    private static String loaderName(ClassLoader loader) {
        return loader == null ? "bootstrap(null)" : loader.getName();
    }

    /** 不覆盖 loadClass，仅在平台加载器找不到目标时由 findClass 提供一份定义。 */
    private static final class SingleNameLoader extends ClassLoader {
        private final Path classFile;

        private SingleNameLoader(String name, Path classFile) {
            super(name, ClassLoader.getPlatformClassLoader());
            this.classFile = classFile;
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (!PLUGIN.equals(name)) {
                throw new ClassNotFoundException(name);
            }
            try {
                byte[] bytes = Files.readAllBytes(classFile);
                return defineClass(name, bytes, 0, bytes.length);
            } catch (IOException exception) {
                throw new ClassNotFoundException("无法读取教学类文件：" + classFile, exception);
            }
        }
    }
}
