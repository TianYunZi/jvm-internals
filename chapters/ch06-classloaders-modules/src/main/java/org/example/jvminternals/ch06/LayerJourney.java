package org.example.jvminternals.ch06;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.Set;

/** CH06-E04：从本地模块创建子层，将模块关系与加载器身份分别观察。 */
public final class LayerJourney {
    private static final System.Logger LOGGER = System.getLogger(LayerJourney.class.getName());

    private LayerJourney() {
    }

    /** 接收含 lesson.library 的编译目录，不使用远程模块。 */
    public static void main(String[] args) throws ReflectiveOperationException {
        if (args.length != 1) {
            throw new IllegalArgumentException("用法：LayerJourney <命名模块输出目录>");
        }
        LOGGER.log(System.Logger.Level.INFO, "开始创建一个本地子模块层");
        ModuleFinder finder = ModuleFinder.of(Path.of(args[0]));
        Configuration configuration = ModuleLayer.boot().configuration()
                .resolve(finder, ModuleFinder.of(), Set.of("lesson.library"));
        ModuleLayer layer = ModuleLayer.boot().defineModulesWithOneLoader(configuration,
                ClassLoader.getSystemClassLoader());
        Module library = layer.findModule("lesson.library").orElseThrow();
        Class<?> type = layer.findLoader("lesson.library")
                .loadClass("org.example.jvminternals.ch06.moduleapi.PublicGreeting");
        System.out.println("caller.named=" + LayerJourney.class.getModule().isNamed());
        System.out.println("library.in.boot=" + ModuleLayer.boot().findModule("lesson.library").isPresent());
        System.out.println("library.in.child=" + (library.getLayer() == layer));
        System.out.println("class.module=" + type.getModule().getName());
        System.out.println("class.loader.type=" + type.getClassLoader().getClass().getName());
        System.out.println("public.result=" + type.getMethod("message").invoke(null));
        LOGGER.log(System.Logger.Level.INFO, "子层观察结束");
    }
}
