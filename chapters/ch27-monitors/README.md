# 第27章：源码与运行

Java开发工具包（Java Development Kit，JDK）25.0.5.0.2，HotSpot、macOS AArch64。容量单位采用兆二进制字节（mebibyte，MiB），每个教学进程堆上限128 MiB。

## 示例、性质与边界

CH27-E01：MonitorJourney，包org.example.jvminternals.ch27。正式源码在src/main/java，阅读副本从正式文件生成；用户“执行P07”授权普通教学源码与有界观察，无测试、断言、并发压力或性能基准。

3个子线程；持锁6秒、wait循环12秒截止；重入两层，类库锁finally按层数释放。JDK飞行记录器（JDK Flight Recorder，JFR）与trace日志会改变观察开销，本例不用于计时排名。

## 可复制命令

在项目根目录执行。先编译普通示例；预览模块与本地编译只在各自命令中显式选择。命令运行按顺序等待本轮进程结束。新运行输出放target，保留本轮精选证据。

```bash
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@25/25.0.5.0.2/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:/opt/homebrew/bin:$PATH"
mvn -B -ntp -pl chapters/ch27-monitors -am compile
jfr configure --input profile --output chapters/ch27-monitors/target/monitors.jfc 'jdk.JavaMonitorEnter#enabled=true' 'jdk.JavaMonitorEnter#threshold=0ms' 'jdk.JavaMonitorWait#enabled=true' 'jdk.JavaMonitorWait#threshold=0ms' 'jdk.JavaMonitorInflate#enabled=true'
java -Xmx128m -XX:+StartAttachListener -XX:StartFlightRecording=settings=chapters/ch27-monitors/target/monitors.jfc,filename=chapters/ch27-monitors/target/monitors.jfr,dumponexit=true '-Xlog:monitorinflation=trace:file=chapters/ch27-monitors/target/monitorinflation.log:uptimemillis,level,tags' -cp chapters/ch27-monitors/target/classes org.example.jvminternals.ch27.MonitorJourney
jfr print --events jdk.JavaMonitorEnter,jdk.JavaMonitorWait,jdk.JavaMonitorInflate chapters/ch27-monitors/target/monitors.jfr
javap -c -p -classpath chapters/ch27-monitors/target/classes org.example.jvminternals.ch27.MonitorJourney
java -Xmx128m -XX:+UnlockDiagnosticVMOptions -XX:+PrintFlagsFinal -version
java -Xmx128m -XX:+UnlockDiagnosticVMOptions -XX:+UseCompactObjectHeaders -XX:+PrintFlagsFinal -version
```

在第二个终端同样设置上述工具环境，按提示输入刚打印的进程号；历史进程号不能复用。

```bash
# 在教学进程的观察窗口内，从其输出取得本次pid。
read task_pid
jcmd "$task_pid" Thread.print -l
```

## 实际观察和清理

pid82095，正常三线程过程退出0。教学对象因Monitor Wait膨胀；竞争者进入事件6.01秒；等待者超时后重新获取等待约5.00秒。转储在通知前，不能解释成通知后执行；未取得教学对象异步收缩事件。独立默认／紧凑参数查询与CH10一致。

运行编号ch27-20261103-01，完整输入、输出、身份摘要和边界见[evidence/runs/ch27-20261103-01/记录.md](evidence/runs/ch27-20261103-01/记录.md)。所有本轮Java进程已结束；手动重跑时正常结束会join教学线程，必要时在该终端Ctrl-C停止当前进程，不寻找或结束其他进程。

## 静态阅读

[正文](chapter.html)、[源码阅读](sources.html)、[证据索引](evidence/索引.md)对应。代码采用Shiki 4.6.0、github-light主题；Java、C（本地源）、bash（命令）及text（混合输出）按实际语言生成静态超文本标记语言（Hypertext Markup Language，HTML）。图源位于figures，软件关系和流程使用Mermaid，空间示意使用可缩放矢量图形（Scalable Vector Graphics，SVG）。图源修改后重新导出，源码修改后重新生成阅读页，不依赖网络渲染。
