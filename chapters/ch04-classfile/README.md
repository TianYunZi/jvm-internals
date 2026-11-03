# 第 4 章示例与运行说明

本章解释类文件怎样表达结构、类型与行为。正文使用超文本标记语言（Hypertext Markup Language，HTML），入口为 [chapter.html](chapter.html)；教学源码、原始观察与图解均在本目录。

浏览器完整源码入口为 [sources.html](sources.html)，由四份正式源文件生成；正式编译输入仍是 `src/main/java/`，以后修改源文件时须同步重新生成阅读副本。该安排使读者无需浏览器直接处理 Java 文件的媒体类型即可阅读全文。

## 环境与输入

本轮实际环境为 Homebrew Java 开发工具包（Java Development Kit，JDK）25.0.5.0.2、Maven 3.10.0、macOS AArch64。详细身份、源码提交与发行版差异见[公共基线](../../执行记录/基线与约定.md)。普通源码不启用预览，没有第三方运行依赖。

以下命令都在**项目根目录**执行。先选择本轮已核对的工具目录：

```sh
CH04_JDK=/opt/homebrew/Cellar/openjdk@25/25.0.5.0.2/libexec/openjdk.jdk/Contents/Home
```

这个本机路径仅是本轮环境选择。其他机器先选择自身已核查的 JDK 25，将 `CH04_JDK` 赋为该工具包的完整主目录；不直接依赖 macOS `java_home` 的默认结果。书稿链接使用相对路径，可搬移阅读。

## 示例清单

用户于 2026 年 11 月 3 日授权执行 P00—P02。下表均为教学源码与结构观察，不承担测试、业务正确性断言、故障复现或性能基准职责；未编写任何测试代码。

| 示例 | 节号与主要问题 | 源文件与入口 | 输入、观察与实际证据 |
| --- | --- | --- | --- |
| CH04-E01 | 4.1—4.3，描述符、泛型、分支及异常表 | [ClassfileSubject.java](src/main/java/org/example/jvminternals/ch04/structure/ClassfileSubject.java)，无进程入口 | 只编译及反汇编，不执行异常方法；[subject.javap.txt](evidence/runs/ch04-20261103-01/subject.javap.txt)、[header.hex.txt](evidence/runs/ch04-20261103-01/header.hex.txt) |
| CH04-E02 | 4.4，现代声明与反射 | [ModernShapes.java](src/main/java/org/example/jvminternals/ch04/modern/ModernShapes.java)，`main` | 无输入，读取固定类型元数据；[modern.javap.txt](evidence/runs/ch04-20261103-01/modern.javap.txt)、[reflection.txt](evidence/runs/ch04-20261103-01/reflection.txt) |
| CH04-E03 | 4.5.1，模型层级与代码元素 | [ClassfileExplorer.java](src/main/java/org/example/jvminternals/ch04/model/ClassfileExplorer.java)，`main` | 指定 E01 类文件；[model.txt](evidence/runs/ch04-20261103-01/model.txt) |
| CH04-E04 | 4.5.2，类文件生成与元素流变换 | [ClassfileWorkshop.java](src/main/java/org/example/jvminternals/ch04/transform/ClassfileWorkshop.java)，`main` | 一个尚不存在的输出目录；[workshop.txt](evidence/runs/ch04-20261103-01/workshop.txt)、两份生成产物反汇编 |

E02 输出反射元数据；E03 只解析指定小型本章文件，不加载目标类；E04 只生成和变换，不定义或执行生成类，也不调用校验器。标准输出是教学数据，`System.Logger` 只记录开始与结束。输出格式中的内部模型标识、日志时间和编译器常量池编号可能变化，不作为稳定接口。

## 最小编译

根构建固定编译插件 3.14.1、资源插件 3.3.1，使用 `--release 25`、`-g:lines,vars,source` 和 `-parameters`。默认构建没有启动教学进程的绑定。

本轮使用已有本地插件缓存，实际命令为：

```sh
JAVA_HOME="$CH04_JDK" mvn --offline --batch-mode --no-transfer-progress -pl chapters/ch04-classfile -am compile
```

退出 0，4 个源文件编译成功，见 [compile.txt](evidence/runs/ch04-20261103-01/compile.txt)。产物目录为 `chapters/ch04-classfile/target/classes/`。

初次使用、缺少本地缓存时，采用同一构建的联网形式；本轮未重新联网下载插件：

```sh
JAVA_HOME="$CH04_JDK" mvn --batch-mode --no-transfer-progress -pl chapters/ch04-classfile -am compile
```

## 反汇编、反射与模型读取

以下为本轮已执行命令的同参数形式；这里直接显示结果，本轮正式原始输出另存于证据目录。

```sh
"$CH04_JDK/bin/javap" -v -p -classpath chapters/ch04-classfile/target/classes org.example.jvminternals.ch04.structure.ClassfileSubject
xxd -g 1 -l 32 chapters/ch04-classfile/target/classes/org/example/jvminternals/ch04/structure/ClassfileSubject.class
"$CH04_JDK/bin/javap" -v -p -classpath chapters/ch04-classfile/target/classes 'org.example.jvminternals.ch04.modern.ModernShapes$Point' 'org.example.jvminternals.ch04.modern.ModernShapes$Shape' 'org.example.jvminternals.ch04.modern.ModernShapes$Member' org.example.jvminternals.ch04.modern.ModernShapes
"$CH04_JDK/bin/java" -Xmx128m -cp chapters/ch04-classfile/target/classes org.example.jvminternals.ch04.modern.ModernShapes
"$CH04_JDK/bin/java" -Xmx128m -cp chapters/ch04-classfile/target/classes org.example.jvminternals.ch04.model.ClassfileExplorer chapters/ch04-classfile/target/classes/org/example/jvminternals/ch04/structure/ClassfileSubject.class
```

本轮观察为：主版本 69、次版本 0；泛型字段描述符为 `Number`，另有 `TT;` 签名；方法有两个栈映射帧；反射显示运行期注解、记录组件及允许子类型。具体索引与偏移以本轮完整输出为准。

## 类文件生成与变换

本轮原命令的输出目录是 `chapters/ch04-classfile/target/workshop-20261103-01`，程序创建 `original/` 与 `transformed/` 两个子目录。两份文件具有同一个二进制名称，不能混在同一类路径中。程序在目录存在时失败而不是覆盖已有产物。

再次生成时，以下命令用系统已有的目录工具选择唯一父目录，再将未存在的 `classfiles` 作为输入。该目录选择形式便于重复操作；本轮正式记录仍使用上面的固定运行目录。

```sh
CH04_OUTPUT="$(mktemp -d chapters/ch04-classfile/target/ch04-reader.XXXXXX)/classfiles"
"$CH04_JDK/bin/java" -Xmx128m -cp chapters/ch04-classfile/target/classes org.example.jvminternals.ch04.transform.ClassfileWorkshop "$CH04_OUTPUT"
"$CH04_JDK/bin/javap" -v -p "$CH04_OUTPUT/original/GeneratedNumber.class"
"$CH04_JDK/bin/javap" -v -p "$CH04_OUTPUT/transformed/GeneratedNumber.class"
```

本轮两份产物均为 190 字节；整数立即数由 ±7 改为 ±9，分支和栈映射形状相同。精选字节已保存在 [classfiles/](evidence/runs/ch04-20261103-01/classfiles/)，不依赖被忽略的 `target/` 长期存在。这里只证明生成与工具观察，未证明类加载、执行或业务正确性。

## 资源、结束与证据

教学进程使用 `-Xmx128m`，不主动创建教学线程，没有循环负载或网络访问；输入固定为本章约 1.3 千字节的类文件，E04 只输出两份 190 字节文件。本轮进程均在工具单次等待内自然退出。堆上限不等于整个进程的内存配额；若人工运行异常滞留，可用终端中断键停止，不通过无限重试扩大负载。

完整记录见 [ch04-20261103-01](evidence/runs/ch04-20261103-01/记录.md)，包含环境、源码摘要、命令、退出情况和解释边界。[证据索引](evidence/索引.md)区分规范、实现、工具观察与推断；[TASK.md](TASK.md)维护本章唯一进度。
