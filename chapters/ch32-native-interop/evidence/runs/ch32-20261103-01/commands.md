# 命令与工作目录

工作目录：`/Users/xiejinxin/Developer/Java/jvm-internals`。实际调用选择显式25版工具；下列为实际参数的可复制重放入口，输出映射见记录；README中的单章Maven命令是等价复现入口，未逐章重复执行。实际Maven命令为三个模块的最小聚合编译。首次Java编译使用javac，随后根变更采用仅三个模块的Maven聚合编译，后者见公共证据。

本章使用 Java 开发工具包（Java Development Kit，JDK）25.0.5.0.2、macOS AArch64和OpenJDK HotSpot。以下命令在项目根目录执行，实际工具固定为Homebrew的25版。普通编译不启动进程；所有观察为有限教学任务，没有测试、断言、压力或性能基准。共享文件和本地二进制是平台相关产物，不保证跨版本搬运。

```bash
P08_JDK=/opt/homebrew/opt/openjdk@25
```

## 教学范围

CH32-E01 NativeJourney与interop.c显示Java本地接口（Java Native Interface，JNI）的引用、短临界区和外部线程。CH32-E02 ForeignJourney使用外部函数与内存（Foreign Function & Memory，FFM）接口。Java源是普通25版接口，本地编译单独执行，不作为根构建自动步骤。示例共享槽仅限串行调用。

## Java与本地编译

```bash
JAVA_HOME="$P08_JDK" /opt/homebrew/bin/mvn -pl chapters/ch32-native-interop -am compile
"$P08_JDK/bin/javac" -g -parameters -h chapters/ch32-native-interop/target/headers -d chapters/ch32-native-interop/target/classes chapters/ch32-native-interop/src/main/java/org/example/jvminternals/ch32/*.java
/usr/bin/clang -dynamiclib -Wall -Wextra -Werror -I /opt/homebrew/opt/openjdk@25/include -I /opt/homebrew/opt/openjdk@25/include/darwin -o chapters/ch32-native-interop/target/libch32.dylib chapters/ch32-native-interop/native/interop.c
P08_NATIVE_LIBRARY="$PWD/chapters/ch32-native-interop/target/libch32.dylib"
```

以上是本机macOS AArch64调用约定，库后缀和头目录不可照搬到其他系统。函数采用固定32位整数，不假定任意平台的C long宽度。

## 显式本地访问

```bash
"$P08_JDK/bin/java" -Xmx64m --enable-native-access=ALL-UNNAMED "-Dch32.library=$P08_NATIVE_LIBRARY" -cp chapters/ch32-native-interop/target/classes org.example.jvminternals.ch32.NativeJourney
"$P08_JDK/bin/java" -Xmx64m --enable-native-access=ALL-UNNAMED "-Dch32.library=$P08_NATIVE_LIBRARY" -cp chapters/ch32-native-interop/target/classes org.example.jvminternals.ch32.ForeignJourney
```

JNI强／弱全局引用在finally中删除；临界区内只读三个整数，不打印、不阻塞、不回调。原生线程取得自己的环境并在回调后分离，由创建方join；本地线程创建失败有错误状态，join失败是不可安全返回的进程边界，不作为故障演示触发。C没有跨返回保留指针。FFM回调同步返回，Arena才关闭；作用域关闭后只查询存活标记，不解引用失效段。

## 25版未授权访问的告警观察

以下是普通受限方法调用，未显式开启访问时本机25版告警后仍执行；没有设置拒绝模式或制造崩溃。

```bash
"$P08_JDK/bin/java" -Xmx64m "-Dch32.library=$P08_NATIVE_LIBRARY" -cp chapters/ch32-native-interop/target/classes org.example.jvminternals.ch32.NativeJourney
"$P08_JDK/bin/java" -Xmx64m "-Dch32.library=$P08_NATIVE_LIBRARY" -cp chapters/ch32-native-interop/target/classes org.example.jvminternals.ch32.ForeignJourney
```

实际输出为临界区求和10、isCopy=false，附加线程回调状态0；下行调用及上行回调结果34，Arena关闭后alive=false。没有主动弱引用回收、错误线程、越界、泄漏或崩溃复现。[运行记录](evidence/runs/ch32-20261103-01/记录.md)保留完整输出及摘要；[源码阅读](sources.html)包含C源。
