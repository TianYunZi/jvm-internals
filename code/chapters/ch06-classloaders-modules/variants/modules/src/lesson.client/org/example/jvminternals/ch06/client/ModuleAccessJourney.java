package org.example.jvminternals.ch06.client;

import java.lang.reflect.Method;
import org.example.jvminternals.ch06.moduleapi.PublicGreeting;

/** CH06-E03：已授权的模块访问失败与单进程导出／开放对照。 */
public final class ModuleAccessJourney {
    private static final System.Logger LOGGER = System.getLogger(ModuleAccessJourney.class.getName());
    private static final String INTERNAL = "org.example.jvminternals.ch06.internal";

    private ModuleAccessJourney() {
    }

    /** 正常找到类后分别观察公开访问与私有成员深反射，不添加断言。 */
    public static void main(String[] args) throws ReflectiveOperationException {
        LOGGER.log(System.Logger.Level.INFO, "开始观察命名模块的访问边界");
        System.out.println("api=" + PublicGreeting.message());
        Class<?> secret = Class.forName(INTERNAL + ".Secret");
        Module caller = ModuleAccessJourney.class.getModule();
        Module library = secret.getModule();
        System.out.println("class.found=" + secret.getName());
        System.out.println("caller.reads.library=" + caller.canRead(library));
        System.out.println("package.exported=" + library.isExported(INTERNAL, caller));
        System.out.println("package.open=" + library.isOpen(INTERNAL, caller));
        Method publicMethod = secret.getMethod("message");
        try {
            System.out.println("public.invoke=" + publicMethod.invoke(null));
        } catch (IllegalAccessException exception) {
            // 访问失败属于本章获授权案例，不等于类没有找到。
            System.out.println("public.invoke=" + exception.getClass().getSimpleName());
        }
        Method privateMethod = secret.getDeclaredMethod("hidden");
        boolean accessible = privateMethod.trySetAccessible();
        System.out.println("private.accessible=" + accessible);
        if (accessible) {
            System.out.println("private.invoke=" + privateMethod.invoke(null));
        }
        LOGGER.log(System.Logger.Level.INFO, "命名模块访问观察结束");
    }
}
