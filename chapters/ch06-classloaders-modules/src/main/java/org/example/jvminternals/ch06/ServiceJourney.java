package org.example.jvminternals.ch06;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ServiceLoader;

/** CH06-E02：临时设置上下文加载器，发现外部提供者并在结束时恢复。 */
public final class ServiceJourney {
    private static final System.Logger LOGGER = System.getLogger(ServiceJourney.class.getName());

    private ServiceJourney() {
    }

    /** 接收仅含本章提供者与 META-INF/services 的编译目录。 */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("用法：ServiceJourney <提供者输出目录>");
        }
        Thread current = Thread.currentThread();
        ClassLoader previous = current.getContextClassLoader();
        URL location = Path.of(args[0]).toUri().toURL();
        LOGGER.log(System.Logger.Level.INFO, "开始使用上下文加载器发现服务提供者");
        try (URLClassLoader providerLoader = new URLClassLoader("greeting-provider",
                new URL[]{location}, ClassLoader.getSystemClassLoader())) {
            current.setContextClassLoader(providerLoader);
            try {
                for (Greeter greeter : ServiceLoader.load(Greeter.class)) {
                    System.out.printf("provider=%s loader=%s service.loader=%s message=%s%n",
                            greeter.getClass().getName(), greeter.getClass().getClassLoader().getName(),
                            Greeter.class.getClassLoader().getName(), greeter.greet());
                }
            } finally {
                // 线程可能比本次发现过程更长寿，必须恢复原上下文引用。
                current.setContextClassLoader(previous);
            }
        }
        System.out.println("context.restored=" + (current.getContextClassLoader() == previous));
        LOGGER.log(System.Logger.Level.INFO, "提供者资源关闭，上下文已恢复；未声称类已卸载");
    }
}
