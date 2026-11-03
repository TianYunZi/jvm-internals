# 第19章 垃圾收集的理论基础：源码与运行

容量单位采用兆二进制字节（mebibyte，MiB）和千二进制字节（kibibyte，KiB）。本章观察垃圾收集（Garbage Collection，GC）机制。

Java开发工具包（Java Development Kit，JDK）25.0.5.0.2，HotSpot、macOS AArch64。CH19-E01 ReferenceJourney：一个1 MiB数组、两次回收请求、两个最长1500毫秒队列窗口；单主线程，另有类库管理的引用／清理线程。用户“执行P06”授权普通教学源码及有界机制观察；没有测试、断言、压力、性能基准或主动资源耗尽。

## 完整命令

在项目根目录执行。编译只编译，不自动运行；结束后进程正常退出，必要时可用终端中断停止。

```sh
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@25/25.0.5.0.2/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:/opt/homebrew/bin:$PATH"
mvn -B -ntp -pl chapters/ch19-gc-foundations -am compile
java -Xms16m -Xmx64m -XX:+UseSerialGC '-Xlog:gc*,gc+ref=debug:file=chapters/ch19-gc-foundations/evidence/runs/ch19-20261103-01/gc.log:uptime,level,tags' -cp chapters/ch19-gc-foundations/target/classes org.example.jvminternals.ch19.ReferenceJourney
```

## 实际记录与边界

本轮收到两类通知，不证明请求即完成、软引用淘汰时点、终结时点或资源归还系统；没有外部资源、耗尽和性能结论。

原始输入、实际观察与退出情况见[evidence/runs/ch19-20261103-01/记录.md](evidence/runs/ch19-20261103-01/记录.md)。[正文](chapter.html)、[源码静态阅读](sources.html)与[证据索引](evidence/索引.md)相互对应。软件交互、流程和状态图用Mermaid维护，空间布局用可缩放矢量图形（Scalable Vector Graphics，SVG）；导出工具与高亮版本沿用公共基线，图源修改后重新导出。
