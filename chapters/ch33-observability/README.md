# 第33章运行说明

本章基于Java开发工具包（Java Development Kit，JDK）25.0.5.0.2、macOS AArch64。以下命令在项目根目录执行，环境变量只用于本任务，不改变系统默认Java。普通构建只编译，不自动启动教学进程。

```bash
P09_JDK=/opt/homebrew/opt/openjdk@25
```

## CH33-E01 最小代理与观察窗口

三个正式Java文件位于src/main/java，变换只匹配ObservedValue。清单声明重转换和重定义；不进行动态代理附加。先编译生成target目录，再执行以下命令写入清单；最后一个空参数保留清单末尾空行。

```bash
JAVA_HOME="$P09_JDK" mvn -pl chapters/ch33-observability -am compile
printf '%s\n' 'Manifest-Version: 1.0' \
  'Premain-Class: org.example.jvminternals.ch33.ObservationAgent' \
  'Can-Retransform-Classes: true' 'Can-Redefine-Classes: true' '' \
  > chapters/ch33-observability/target/agent.mf
```

```bash
"$P09_JDK/bin/jar" --create --file chapters/ch33-observability/target/observation-agent.jar --manifest chapters/ch33-observability/target/agent.mf -C chapters/ch33-observability/target/classes .
"$P09_JDK/bin/java" -Xmx64m -XX:+StartAttachListener -XX:NativeMemoryTracking=summary -javaagent:chapters/ch33-observability/target/observation-agent.jar '-Xlog:class+load=info,safepoint=info:file=chapters/ch33-observability/target/runtime.log:uptime,level,tags,tid:filecount=2,filesize=1m' -cp chapters/ch33-observability/target/classes org.example.jvminternals.ch33.ObservationJourney
```

窗口只有十二秒，另一个终端以输出的进程号替换下列变量。不要把别的Java进程作为目标。命令是否支持以目标help为准。

```bash
P09_PID=实际输出的进程号
"$P09_JDK/bin/jcmd" "$P09_PID" help
"$P09_JDK/bin/jcmd" "$P09_PID" Thread.print -l
"$P09_JDK/bin/jcmd" "$P09_PID" Thread.dump_to_file -format=json chapters/ch33-observability/target/threads.json
"$P09_JDK/bin/jcmd" "$P09_PID" VM.native_memory summary
"$P09_JDK/bin/jcmd" "$P09_PID" PerfCounter.print
"$P09_JDK/bin/jcmd" "$P09_PID" VM.log what=gc=info
```

JavaScript对象表示法（JavaScript Object Notation，JSON）转储包含本例虚拟线程，传统转储侧重平台线程和锁；本地内存跟踪（Native Memory Tracking，NMT）需要从启动时开启。不要从附加失败推导进程内容为空。

同一变换现象还以飞行记录事件观察；应用有界结束后再分析。

```bash
"$P09_JDK/bin/java" -Xmx64m -XX:StartFlightRecording=settings=profile,filename=chapters/ch33-observability/target/observation.jfr -javaagent:chapters/ch33-observability/target/observation-agent.jar '-Xlog:redefine+class*=debug:file=chapters/ch33-observability/target/redefine.log:uptime,level,tags' -cp chapters/ch33-observability/target/classes org.example.jvminternals.ch33.ObservationJourney
"$P09_JDK/bin/jfr" print --events 'jdk.RedefineClasses,jdk.RetransformClasses' chapters/ch33-observability/target/observation.jfr
```

本章没有测试、断言或故障复现。三次运行的原始输出分别保存，第三次专门补充事件；记录见evidence/runs。
