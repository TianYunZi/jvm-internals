# 第 3 章观察与调试说明

正文见 [chapter.html](chapter.html)。本章复用 [CH01 ProgramJourney](../ch01-architecture/README.md) 的正式源码与编译产物；目标诊断能力明确复用 [CH02-O02](../ch02-computer-basics/evidence/runs/ch02-20261103-02/记录.md)，不新增同质 Java 程序或空构建模块。

## 授权与条件

用户已明确授权执行 P03。本章使用现有工具读取参数、记录事件并在自己的普通进程上观察三个本地断点；没有测试框架、断言或基准。实际 Java 开发工具包（Java Development Kit，JDK）为 Homebrew 25.0.5.0.2，主平台 macOS AArch64，产品构建 release。

先按第 1 章在项目根目录编译，选择同一工具目录：

```sh
CH03_JDK=/opt/homebrew/Cellar/openjdk@25/25.0.5.0.2/libexec/openjdk.jdk/Contents/Home
```

## 普通解释配置与工具帮助

本轮在项目根目录执行：

```sh
"$CH03_JDK/bin/java" -Xint -XX:-UseTLAB -Xms32m -Xmx128m -Xlog:class+load=info:file=chapters/ch03-source-environment/evidence/runs/ch03-20261103-01/class-load.log:uptime,level,tags -cp chapters/ch01-architecture/target/classes org.example.jvminternals.ch01.ProgramJourney chapters/ch01-architecture/src/main/resources/message.txt 0
"$CH03_JDK/bin/java" -XX:+PrintFlagsFinal -version
"$CH03_JDK/bin/java" -XX:+UnlockDiagnosticVMOptions -XX:+UnlockExperimentalVMOptions -XX:+PrintFlagsFinal -version
"$CH03_JDK/bin/java" -Xlog:help
```

实际输出分别保存在本轮记录的 program.txt、default-flags.txt、unlocked-flags.txt、log-help.txt。再次留证应选择新目录，避免覆盖正式日志。两个参数表来自独立的 `-version` 进程，不与 CH02 的显式堆配置混用。

目标进程的 `help`、VM.flags、VM.info 来自 CH02-O02，实际进程号是当时的 15216，现已结束。新的目标需从其本轮输出取得实际进程号；不使用 pid=0 或模糊类名向其他进程广播命令。

## 本地断点

本轮使用系统已有的 `lldb`，在允许调试本进程的环境中执行以下命令。它跳过用户初始化文件；保留地址随机化；每个断点只观察一次，随后禁用并继续到自然退出。

```sh
lldb --batch --no-lldbinit \
  -o 'settings set target.disable-aslr false' \
  -o 'breakpoint set --name JNI_CreateJavaVM' -o run -o 'bt 6' \
  -o 'breakpoint disable 1' \
  -o 'breakpoint set --name ClassFileParser::parse_stream' -o continue -o 'bt 6' \
  -o 'breakpoint disable 2' \
  -o 'breakpoint set --name InterpreterRuntime::_new' -o continue -o 'bt 6' \
  -o 'breakpoint disable 3' -o continue \
  -- "$CH03_JDK/bin/java" -Xint -XX:-UseTLAB -Xms32m -Xmx128m \
  -cp chapters/ch01-architecture/target/classes \
  org.example.jvminternals.ch01.ProgramJourney \
  chapters/ch01-architecture/src/main/resources/message.txt 0
```

实际已命中 libjvm.dylib 中的三个入口，解析与分配命中均在应用 main-enter 之前；没有记录断点参数中的具体类名，不能把它们说成 Payload 的指定事件。原始结果在 [lldb.txt](evidence/runs/ch03-20261103-01/lldb.txt)。首次创建断点有同名模块位置，实际停靠模块需对照输出。

关闭线程本地分配缓冲区（Thread-Local Allocation Buffer，TLAB）与解释配置是为了明确阅读路径，不作性能选型建议。调试停顿改变执行条件，不用其相对阶段耗时和普通运行比较性能。其他发行包若剥离相应符号，需要对应符号材料；本轮没有安装 fastdebug 或 slowdebug。

## 结束、资源与证据

普通进程观察秒数为 0；工作线程有界退出，堆参数 32—128 兆二进制字节（mebibyte，MiB）。调试器批处理执行到目标退出 0，未遗留被暂停进程；进程总内存不等于堆上限。所有观察目的、身份、命令与解释边界见 [CH03-O01](evidence/runs/ch03-20261103-01/记录.md)，精确源码入口见[来源索引](evidence/索引.md)。
