# 第20章 安全点、线程握手与垃圾收集屏障：源码与运行

容量单位采用兆二进制字节（mebibyte，MiB）和千二进制字节（kibibyte，KiB）。本章观察垃圾收集（Garbage Collection，GC）机制。

Java开发工具包（Java Development Kit，JDK）25.0.5.0.2，HotSpot、macOS AArch64。CH20-E01 SafepointJourney：24批、每批64个8192字节数组，每批休眠25毫秒，主线程两次请求并join收尾。用户“执行P06”授权普通教学源码及有界机制观察；没有测试、断言、压力、性能基准或主动资源耗尽。

## 完整命令

在项目根目录执行。编译只编译，不自动运行；结束后进程正常退出，必要时可用终端中断停止。

```sh
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@25/25.0.5.0.2/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:/opt/homebrew/bin:$PATH"
mvn -B -ntp -pl chapters/ch20-safepoints-barriers -am compile
java -Xms16m -Xmx64m -XX:+UseG1GC '-Xlog:gc*,safepoint=debug,handshake=debug:file=chapters/ch20-safepoints-barriers/evidence/runs/ch20-20261103-01/runtime.log:uptimemillis,level,tags' -cp chapters/ch20-safepoints-barriers/target/classes org.example.jvminternals.ch20.SafepointJourney
```

## 实际记录与边界

本次只关联实际记录的协调与回收；没有制造慢安全点、线程故障或握手压力，没有测业务尾延迟；握手和逐槽修复从固定实现解释。

原始输入、实际观察与退出情况见[evidence/runs/ch20-20261103-01/记录.md](evidence/runs/ch20-20261103-01/记录.md)。[正文](chapter.html)、[源码静态阅读](sources.html)与[证据索引](evidence/索引.md)相互对应。软件交互、流程和状态图用Mermaid维护，空间布局用可缩放矢量图形（Scalable Vector Graphics，SVG）；导出工具与高亮版本沿用公共基线，图源修改后重新导出。
