# 第24章 Shenandoah 与分代演进：源码与运行

容量单位采用兆二进制字节（mebibyte，MiB）和千二进制字节（kibibyte，KiB）。本章观察垃圾收集（Garbage Collection，GC）机制。

Java开发工具包（Java Development Kit，JDK）25.0.5.0.2，HotSpot、macOS AArch64。CH24-O01复用CH23-E01 ConcurrentCycleJourney正式源码与类文件；本章无新Java编译模块。用户“执行P06”授权普通教学源码及有界机制观察；没有测试、断言、压力、性能基准或主动资源耗尽。

## 完整命令

在项目根目录执行。编译只编译，不自动运行；结束后进程正常退出，必要时可用终端中断停止。

```sh
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@25/25.0.5.0.2/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:/opt/homebrew/bin:$PATH"
mvn -B -ntp -pl chapters/ch23-zgc -am compile
java -Xms128m -Xmx128m -XX:ActiveProcessorCount=2 -XX:ParallelGCThreads=2 -XX:ConcGCThreads=1 -XX:+UseShenandoahGC -XX:ShenandoahGCMode=satb '-Xlog:gc*,gc+age=trace,gc+stats=debug:file=chapters/ch24-shenandoah/evidence/runs/ch24-20261103-01/satb.log:uptimemillis,level,tags' -cp chapters/ch23-zgc/target/classes org.example.jvminternals.ch23.ConcurrentCycleJourney
java -Xms128m -Xmx128m -XX:ActiveProcessorCount=2 -XX:ParallelGCThreads=2 -XX:ConcGCThreads=1 -XX:+UseShenandoahGC -XX:ShenandoahGCMode=generational '-Xlog:gc*,gc+age=trace,gc+stats=debug:file=chapters/ch24-shenandoah/evidence/runs/ch24-20261103-01/generational.log:uptimemillis,level,tags' -cp chapters/ch23-zgc/target/classes org.example.jvminternals.ch23.ConcurrentCycleJourney
java -XX:+UseShenandoahGC -XX:+PrintFlagsFinal -version
```

## 实际记录与边界

两模式可用性、正常并发阶段与晋升已观察；退化／Full计数均0，失败升级路径按源码解释，未主动制造故障或性能基准；其他平台未实测。

原始输入、实际观察与退出情况见[evidence/runs/ch24-20261103-01/记录.md](evidence/runs/ch24-20261103-01/记录.md)。[正文](chapter.html)、[源码静态阅读](sources.html)与[证据索引](evidence/索引.md)相互对应。软件交互、流程和状态图用Mermaid维护，空间布局用可缩放矢量图形（Scalable Vector Graphics，SVG）；导出工具与高亮版本沿用公共基线，图源修改后重新导出。
