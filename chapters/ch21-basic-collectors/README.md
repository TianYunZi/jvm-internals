# 第21章 Serial、Parallel 与 Epsilon：源码与运行

容量单位采用兆二进制字节（mebibyte，MiB）和千二进制字节（kibibyte，KiB）。本章观察垃圾收集（Garbage Collection，GC）机制。

Java开发工具包（Java Development Kit，JDK）25.0.5.0.2，HotSpot、macOS AArch64。CH21-E01 AllocationJourney：4096个16 KiB数组、64槽环形持有、两个200毫秒阶段间隔，单主线程。用户“执行P06”授权普通教学源码及有界机制观察；没有测试、断言、压力、性能基准或主动资源耗尽。

## 完整命令

在项目根目录执行。编译只编译，不自动运行；结束后进程正常退出，必要时可用终端中断停止。

```sh
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@25/25.0.5.0.2/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:/opt/homebrew/bin:$PATH"
mvn -B -ntp -pl chapters/ch21-basic-collectors -am compile
java -Xms128m -Xmx128m -XX:ActiveProcessorCount=2 -XX:+UseSerialGC '-Xlog:gc*,gc+age=trace,gc+ergo=trace:file=chapters/ch21-basic-collectors/evidence/runs/ch21-20261103-01/Serial.log:uptime,level,tags' -cp chapters/ch21-basic-collectors/target/classes org.example.jvminternals.ch21.AllocationJourney
java -Xms128m -Xmx128m -XX:ActiveProcessorCount=2 -XX:+UseParallelGC '-Xlog:gc*,gc+age=trace,gc+ergo=trace:file=chapters/ch21-basic-collectors/evidence/runs/ch21-20261103-01/Parallel.log:uptime,level,tags' -cp chapters/ch21-basic-collectors/target/classes org.example.jvminternals.ch21.AllocationJourney
java -Xms128m -Xmx128m -XX:ActiveProcessorCount=2 -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC '-Xlog:gc*,gc+age=trace,gc+ergo=trace:file=chapters/ch21-basic-collectors/evidence/runs/ch21-20261103-01/Epsilon.log:uptime,level,tags' -cp chapters/ch21-basic-collectors/target/classes org.example.jvminternals.ch21.AllocationJourney
```

## 实际记录与边界

本轮不是性能基准；Epsilon在预算内自然结束，没有资源耗尽记录；晋升失败等分支按固定源码分析，不能标成已触发。

原始输入、实际观察与退出情况见[evidence/runs/ch21-20261103-01/记录.md](evidence/runs/ch21-20261103-01/记录.md)。[正文](chapter.html)、[源码静态阅读](sources.html)与[证据索引](evidence/索引.md)相互对应。软件交互、流程和状态图用Mermaid维护，空间布局用可缩放矢量图形（Scalable Vector Graphics，SVG）；导出工具与高亮版本沿用公共基线，图源修改后重新导出。
