# 第13章 解释器、栈帧与异常执行：源码与运行

Java开发工具包（Java Development Kit，JDK）25.0.5.0.2、HotSpot、macOS AArch64；工具显式选用下列目录。源码为本章src/main/java，页面阅读副本为[sources.html](sources.html)。

## 示例与授权

CH13-E01 ExceptionJourney：两个已捕获异常，固定2次、单线程、堆上限64兆二进制字节（mebibyte，MiB），不足1秒正常结束；源码main→middle→leaf。

用户“执行P05”授权普通教学源码与有界机制观察；不包含测试框架、断言、故障耗尽或性能基准。只打印阶段与必要结果，不在热点内打印。所有命令在项目根目录执行；编译不自动运行进程。

## 完整命令

```sh
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@25/25.0.5.0.2/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:/opt/homebrew/bin:$PATH"
mvn -B -ntp -pl chapters/ch13-interpreter -am compile
java -Xint -Xms16m -Xmx64m -Xlog:exceptions=info:file=chapters/ch13-interpreter/evidence/runs/ch13-20261103-01/exceptions.log:uptime,level,tags -cp chapters/ch13-interpreter/target/classes org.example.jvminternals.ch13.ExceptionJourney
javap -c -v -p -classpath chapters/ch13-interpreter/target/classes org.example.jvminternals.ch13.ExceptionJourney
```

## 实际观察与边界

两次先执行middle的finally，再在main捕获。leaf抛出字节码索引（Bytecode Index，BCI）分别13／24，middle调用BCI 1、catch-all 19、重抛33，main调用BCI 16、匹配处理器22。stdout.txt保留创建时栈与正常结束，exceptions.log与javap.txt共同支持传播路径。

解释模式只固定本轮执行方式，未实测编译形态物理帧布局。异常创建时栈不等于捕获时活动栈。Java Native Interface（JNI）完整边界由CH32承接，内联恢复由CH18承接。

原始文本及退出情况见[evidence/runs/ch13-20261103-01/记录.md](evidence/runs/ch13-20261103-01/记录.md)。观察是具体配置的结果，不提供性能排名或跨平台保证。正式正文、本章配图、源码与相应编号在[正文](chapter.html)对照。

## 配图源与本地导出

软件流程、交互、状态和协作图以figures中的Mermaid源文件维护，帧布局使用可缩放矢量图形（Scalable Vector Graphics，SVG）；逐节分类见TASK.md。当前本机Mermaid命令行接口（Command-Line Interface，CLI）工具为12.2.0。下面在项目根目录只重新导出当前章节图源，不启动教学进程或测试；需要可启动本地离屏渲染器的权限。

```sh
for p05_diagram in chapters/ch13-interpreter/figures/*.mmd; do
  mmdc -i "$p05_diagram" -o "${p05_diagram%.mmd}.svg" -c assets/mermaid.json -b '#f7fafc' -I "$(basename "${p05_diagram%.mmd}")" --no-font-embed
done
```

图源修订后必须重新导出；正文图片的自然宽度与打印宽度按新viewBox同步，相关尺寸记录见公共P05证据。截图只作验收证据，不替代绘制的图。
