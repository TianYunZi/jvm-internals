# 第 9 章运行说明

本章为观察复用章，不创建空构建模块。Java 开发工具包（Java Development Kit，JDK）25.0.5.0.2、macOS AArch64；正式超文本标记语言（Hypertext Markup Language，HTML）正文见 [chapter.html](chapter.html)。

| 示例 | 性质、输入与授权 | 源码及证据 |
| --- | --- | --- |
| CH09-O01 | 普通对象、线程、类活动的进程账本观察；随用户“执行P04”授权复用；无新代码或新运行 | CH01-E01 ProgramJourney、message.txt；CH02-O02 同窗口采集 |

源文件入口为 `../ch01-architecture/src/main/java/org/example/jvminternals/ch01/ProgramJourney.java`，阅读页为 [CH01 源码](../ch01-architecture/sources.html)。没有变更。原编译为 CH01-O01，当前源与类文件摘要复核见本章 evidence/runs/ch09-20261103-01/。复用既有编译和观测，不用一次重新编译来冒充旧进程的输入。

原进程堆初始 32、上限 128 兆二进制字节（mebibyte，MiB），只增加一个教学工作线程，观察窗口 60 秒后自然退出。命令工作目录为项目根目录：

```sh
task_jdk=/opt/homebrew/Cellar/openjdk@25/25.0.5.0.2/libexec/openjdk.jdk/Contents/Home
JAVA_HOME="$task_jdk" mvn -B -ntp -pl chapters/ch01-architecture -am compile
"$task_jdk/bin/java" -Xms32m -Xmx128m -XX:NativeMemoryTracking=detail -cp chapters/ch01-architecture/target/classes org.example.jvminternals.ch01.ProgramJourney chapters/ch01-architecture/src/main/resources/message.txt 60
# 从上述程序的阶段输出取得实际进程号，用它替换 TASK_PID；不要重用历史 15216。
ps -p TASK_PID -o pid,ppid,%cpu,rss,vsz,etime,command
vmmap -summary TASK_PID
vmmap TASK_PID
"$task_jdk/bin/jcmd" TASK_PID GC.heap_info
"$task_jdk/bin/jcmd" TASK_PID VM.native_memory detail scale=KB
"$task_jdk/bin/jcmd" TASK_PID VM.flags
```

本地内存跟踪（Native Memory Tracking，NMT）必须从启动启用；附加需要本机权限。原运行还启用了线程日志并采集线程，完整历史命令以 [CH02-O02](../ch02-computer-basics/evidence/runs/ch02-20261103-02/记录.md) 为准。预期比较的是不同账目范围，不预设数值或驻留比例；本轮实值、时间和限制见[本章复核记录](evidence/runs/ch09-20261103-01/记录.md)。提前停止只针对自己新启动的进程，不操作历史编号或其他 Java 程序。

容器终止、资源耗尽和栈溢出本轮仅解释机制，没有编写或运行故障代码。这里也没有压力、性能基准、断言或测试脚本。
