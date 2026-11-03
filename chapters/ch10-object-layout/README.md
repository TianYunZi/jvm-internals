# 第 10 章运行说明

Java 开发工具包（Java Development Kit，JDK）25.0.5.0.2、Maven 3.10.0、macOS AArch64。正文见 [chapter.html](chapter.html)，教学源阅读副本见 [sources.html](sources.html)。

| 示例 | 性质／授权／节号 | 输入、资源与结束 |
| --- | --- | --- |
| CH10-E01 LayoutJourney | 普通布局观察，随“执行P04”授权；10.1—10.5；没有断言、故障或性能基准 | 九种固定样本、三对象共享图、哈希前后；堆32—128兆二进制字节（mebibyte，MiB）；不创建教学工作线程或网络，有限输出后退出 |

源码为 src/main/java/org/example/jvminternals/ch10/LayoutJourney.java；字段由工具反射读取，继承字段重排是教学内容，不依赖源码声明顺序。提前停止只中断自己启动的教学进程。

## 固定工具与构建

Java Object Layout（JOL）使用官方提交 db2cc744485031c8149991a004ab02f2ea9b24dd 的0.18-SNAPSHOT核心生产源，本地坐标0.18-db2cc744不代表正式版本。工具与源归档、许可证保留于 evidence/tools，完整重建方式见[工具构建说明](evidence/tools/构建说明.md)。原始运行身份和限制见[成功记录](evidence/runs/ch10-20261103-02/记录.md)。

工作目录为项目根目录。先安装随书固定工具，再编译本章：

```sh
task_jdk=/opt/homebrew/Cellar/openjdk@25/25.0.5.0.2/libexec/openjdk.jdk/Contents/Home
JAVA_HOME="$task_jdk" mvn -B -ntp -N org.apache.maven.plugins:maven-install-plugin:3.2.0:install-file -Dfile=chapters/ch10-object-layout/evidence/tools/jol-core-db2cc744.jar -DgroupId=org.openjdk.jol -DartifactId=jol-core -Dversion=0.18-db2cc744 -Dpackaging=jar -DgeneratePom=true
JAVA_HOME="$task_jdk" mvn -B -ntp -pl chapters/ch10-object-layout -am compile
"$task_jdk/bin/java" -Xms32m -Xmx128m -javaagent:chapters/ch10-object-layout/evidence/tools/jol-core-db2cc744.jar -Djol.skipHotspotSAAttach=true -XX:+PrintFlagsFinal -Xlog:gc+heap+coops=debug -cp chapters/ch10-object-layout/target/classes:chapters/ch10-object-layout/evidence/tools/jol-core-db2cc744.jar org.example.jvminternals.ch10.LayoutJourney
"$task_jdk/bin/java" -Xms32m -Xmx128m -XX:+UseCompactObjectHeaders -javaagent:chapters/ch10-object-layout/evidence/tools/jol-core-db2cc744.jar -Djol.skipHotspotSAAttach=true -XX:+PrintFlagsFinal -Xlog:gc+heap+coops=debug -cp chapters/ch10-object-layout/target/classes:chapters/ch10-object-layout/evidence/tools/jol-core-db2cc744.jar org.example.jvminternals.ch10.LayoutJourney
```

本轮输出依次保存 default.txt、compact.txt。工具自身的启动代理用于对象大小支持，不是本书编写的测试代理。跳过 HotSpot 服务性代理（Serviceability Agent，SA）附加后，猜测的地址／基址／位移不可当实测。JOL 的状态文本仍含旧规则，只用原始头字节、字段偏移、数组偏移和大小；当前位图由锁定 HotSpot 源码解释，产品状态由官方设计和实际参数判断。

监视器表是诊断参数，普通管理接口查询未解锁时不可用。补充启动查询显式解锁，仅显示配置，不运行锁竞争：

```sh
"$task_jdk/bin/java" -Xms32m -Xmx128m -XX:+UnlockDiagnosticVMOptions -XX:+PrintFlagsFinal -Xlog:metaspace=info,gc+heap+coops=debug -version
"$task_jdk/bin/java" -Xms32m -Xmx128m -XX:+UseCompactObjectHeaders -XX:+UnlockDiagnosticVMOptions -XX:+PrintFlagsFinal -Xlog:metaspace=info,gc+heap+coops=debug -version
```

两配置预期需要观察不同头部、可能相同的最终大小，而非预设所有对象都节省固定字节。当前结果只适用于这些参数和类型，不是性能、驻留或锁路径的验证。
