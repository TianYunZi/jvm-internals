# 第15章 动态调用、反射与运行时生成：源码与运行

Java开发工具包（Java Development Kit，JDK）25.0.5.0.2、HotSpot、macOS AArch64；工具显式选用下列目录。源码为本章src/main/java，页面阅读副本为[sources.html](sources.html)。

## 示例与授权

CH15-E01 DynamicJourney包含句柄、Lambda／拼接及反射／代理；CH15-E02由同一源generateClass生成真实动态调用／常量类。均为普通教学，单线程、少量固定调用、堆128兆二进制字节（mebibyte，MiB），正常结束。

用户“执行P05”授权普通教学源码与有界机制观察；不包含测试框架、断言、故障耗尽或性能基准。只打印阶段与必要结果，不在热点内打印。所有命令在项目根目录执行；编译不自动运行进程。

## 完整命令

```sh
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@25/25.0.5.0.2/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:/opt/homebrew/bin:$PATH"
mvn -B -ntp -pl chapters/ch15-dynamic-invocation -am compile
java -Xms16m -Xmx128m -Xlog:class+load=info:file=chapters/ch15-dynamic-invocation/evidence/runs/ch15-20261103-01/classes.log:uptime,level,tags -cp chapters/ch15-dynamic-invocation/target/classes org.example.jvminternals.ch15.DynamicJourney
javap -c -v -p chapters/ch15-dynamic-invocation/target/generated/GeneratedLinkage.class
javap -c -v -p -classpath chapters/ch15-dynamic-invocation/target/classes org.example.jvminternals.ch15.DynamicJourney
```

## 实际观察与边界

stdout.txt保留句柄12／12、Lambda12／13与isHidden=true、反射／代理相同目标结果；调用点引导后初始、后续及更新目标，常量引导后复用相同值。generated-javap.txt含InvokeDynamic、Dynamic和两项BootstrapMethods，javap.txt含LambdaMetafactory与StringConcatFactory；classes.log保留加载来源。生成类的精选二进制随本记录保留。

JDK增强提案（JDK Enhancement Proposal，JEP）416官方页面本轮返回403，未以未读页面支撑结论；反射机制直接核查锁定源码。没有计时、引导失败或卸载实验；首次与后续只按事件阶段区分。CH08／CH12持有链边界适用。

原始文本及退出情况见[evidence/runs/ch15-20261103-01/记录.md](evidence/runs/ch15-20261103-01/记录.md)。观察是具体配置的结果，不提供性能排名或跨平台保证。正式正文、本章配图、源码与相应编号在[正文](chapter.html)对照。

## 配图源与本地导出

软件流程、交互、状态和协作图以figures中的Mermaid源文件维护，帧布局使用可缩放矢量图形（Scalable Vector Graphics，SVG）；逐节分类见TASK.md。当前本机Mermaid命令行接口（Command-Line Interface，CLI）工具为12.2.0。下面在项目根目录只重新导出当前章节图源，不启动教学进程或测试；需要可启动本地离屏渲染器的权限。

```sh
for p05_diagram in chapters/ch15-dynamic-invocation/figures/*.mmd; do
  mmdc -i "$p05_diagram" -o "${p05_diagram%.mmd}.svg" -c assets/mermaid.json -b '#f7fafc' -I "$(basename "${p05_diagram%.mmd}")" --no-font-embed
done
```

图源修订后必须重新导出；正文图片的自然宽度与打印宽度按新viewBox同步，相关尺寸记录见公共P05证据。截图只作验收证据，不替代绘制的图。
