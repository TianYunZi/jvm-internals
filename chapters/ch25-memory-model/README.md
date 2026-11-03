# 第25章：源码与运行

Java开发工具包（Java Development Kit，JDK）25.0.5.0.2，HotSpot、macOS AArch64。容量单位采用兆二进制字节（mebibyte，MiB），每个教学进程堆上限128 MiB。

## 示例、性质与边界

CH25-E01：PublicationJourney，包org.example.jvminternals.ch25。正式源码在src/main/java，阅读副本从正式文件生成；用户“执行P07”授权普通教学源码与有界观察，无测试、断言、并发压力或性能基准。

2个子线程；发布等待上限2秒；正常join全部线程。代码片段直接对应发布者；javap不是机器码。

## 可复制命令

在项目根目录执行。先编译普通示例；预览模块与本地编译只在各自命令中显式选择。命令运行按顺序等待本轮进程结束。新运行输出放target，保留本轮精选证据。

```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@25/25.0.5.0.2/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:/opt/homebrew/bin:$PATH"
mvn -B -ntp -pl chapters/ch25-memory-model -am compile
java -Xmx128m -cp chapters/ch25-memory-model/target/classes org.example.jvminternals.ch25.PublicationJourney
javap -c -p -classpath chapters/ch25-memory-model/target/classes org.example.jvminternals.ch25.PublicationJourney
```

## 实际观察和清理

输出payload=42、final=7、count=2、ready=true；退出0。保留javap。有限运行只记事实，正确性来自动作关系推理；没有并发压力或新增反汇编。

运行编号ch25-20261103-01，完整输入、输出、身份摘要和边界见[evidence/runs/ch25-20261103-01/记录.md](evidence/runs/ch25-20261103-01/记录.md)。所有本轮Java进程已结束；手动重跑时正常结束会join教学线程，必要时在该终端Ctrl-C停止当前进程，不寻找或结束其他进程。

## 静态阅读

[正文](chapter.html)、[源码阅读](sources.html)、[证据索引](evidence/索引.md)对应。代码采用Shiki 4.6.0、github-light主题；Java、C（本地源）、bash（命令）及text（混合输出）按实际语言生成静态超文本标记语言（Hypertext Markup Language，HTML）。图源位于figures，软件关系和流程使用Mermaid，空间示意使用可缩放矢量图形（Scalable Vector Graphics，SVG）。图源修改后重新导出，源码修改后重新生成阅读页，不依赖网络渲染。
