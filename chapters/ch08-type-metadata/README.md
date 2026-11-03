# 第 8 章运行说明

Java 开发工具包（Java Development Kit，JDK）25.0.5.0.2、Maven 3.10.0，macOS AArch64。工作目录为项目根目录；普通编译无外部依赖或预览选项。教学源码与正常观测随 P03 授权，没有测试、断言、压力或故障输入。正文见 [chapter.html](chapter.html)，实际源文件的阅读副本见 [sources.html](sources.html)。

| 编号 | 入口／性质／对应节 | 输入、边界与结束 |
| --- | --- | --- |
| CH08-E01 | TypeJourney；正常类型／反射观察；8.1—8.3 | 固定一个 Item 与类型对象，描述项按名字排序；不推断字段物理偏移 |
| CH08-E02 | HiddenJourney；合法生成、隐藏定义及调用；8.4 | 一个输出文件参数，一份类文件、一个隐藏类；NESTMATE，未指定 STRONG；不触发名字发现失败 |
| CH08-E03 | UnloadingJourney；正常持有／释放观察；8.5 | 一个隔离目录参数，只创建一个加载器及一个 Disposable，关闭文件；静态实例先持有后清除；仅两次 System.gc，队列等待至多 2 秒 |

每次堆上限 128 MiB，下限 32 MiB；不创建教学工作线程、网络或持续分配。程序完成有限动作后退出。System.gc 不提供指定对象回收保证，未出现事件也应照实记录。提前结束只中断本任务进程，不终止其他 Java 程序。

## 完整命令

先创建输出记录目录（本项目已存在），再依次执行：

```sh
task_jdk=/opt/homebrew/Cellar/openjdk@25/25.0.5.0.2/libexec/openjdk.jdk/Contents/Home
JAVA_HOME="$task_jdk" mvn -B -ntp -pl chapters/ch08-type-metadata -am compile
"$task_jdk/bin/javac" --release 25 -g -parameters -d chapters/ch08-type-metadata/target/disposable chapters/ch08-type-metadata/variants/disposable/org/example/jvminternals/ch08/disposable/Disposable.java
"$task_jdk/bin/java" -Xms32m -Xmx128m -cp chapters/ch08-type-metadata/target/classes org.example.jvminternals.ch08.TypeJourney
"$task_jdk/bin/java" -Xms32m -Xmx128m -Xlog:class+load=info:file=chapters/ch08-type-metadata/evidence/runs/ch08-20261103-01/hidden-class.log -cp chapters/ch08-type-metadata/target/classes org.example.jvminternals.ch08.HiddenJourney chapters/ch08-type-metadata/evidence/runs/ch08-20261103-01/HiddenValue.class
"$task_jdk/bin/java" -Xms32m -Xmx128m -Xlog:class+load=info,class+unload=info,gc=info,gc+metaspace=info:file=chapters/ch08-type-metadata/evidence/runs/ch08-20261103-01/unloading.log -cp chapters/ch08-type-metadata/target/classes org.example.jvminternals.ch08.UnloadingJourney chapters/ch08-type-metadata/target/disposable
"$task_jdk/bin/javap" -v -p -c chapters/ch08-type-metadata/evidence/runs/ch08-20261103-01/HiddenValue.class
"$task_jdk/bin/javap" -v -p -c -classpath chapters/ch08-type-metadata/target/classes 'org.example.jvminternals.ch08.TypeJourney$Item'
```

Disposable 不在主输出，主程序仅用字符串请求子加载器定义。若把变体加入主路径或保留其他强引用，就改变了观察条件。隐藏类保存的是定义前输入，文件名和运行时隐藏名称分别解释；生成字节使用类文件应用程序编程接口（Application Programming Interface，API），没有手工伪造私有布局。

实际命令将三程序的合并输出依次写入 types.txt、hidden.txt、unloading.txt，两个反汇编写入 hidden-template.javap.txt、item.javap.txt；日志参数已经给出真实保存路径。完整时间、退出与摘要见 [运行记录](evidence/runs/ch08-20261103-01/记录.md)。不要将本轮元空间总量或隐藏名称后缀写成跨运行固定预期。
