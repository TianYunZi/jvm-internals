# 静态代码高亮工具

本工具将书稿代码交给Shiki生成超文本标记语言（Hypertext Markup Language，HTML），页面直接包含带颜色的静态词元；读者离线阅读无需运行作者工具。

锁定Shiki 4.6.0、主题github-light及Marked 17.0.5，精确依赖由package-lock.json维护。Java正文与源码使用java；终端命令使用bash；配置支持xml、json；本地源码符号可用cpp，汇编指令用asm。字节码、日志、路径及混合输出明确使用text，不伪装成可运行源语言。配置在config.json，当前生成范围只含P05；其他章节由对应任务或全书整合统一处理。

在本目录执行：

```sh
npm ci --ignore-scripts --no-audit --no-fund
npm run generate:p05
```

来源页直接读取正式Java文件。正文代码从已有代码节点取出真实文本后交给Shiki；运行说明的Markdown输入转换为README.html，命令代码按围栏语言处理。正文仍以chapter.html为维护源，重复生成会重新取出其代码文本再高亮，不维护第二份正文。生成记录在执行记录/evidence/p05-20261103/shiki-generation.json，原始片段文本在artifacts/p05-code-copy/input供浏览器选取文本核对。

生成动作不会编译或运行教学程序，没有测试、断言或自动质量判断。视觉、复制和静态差异核对另留实际记录。根据[Shiki官方用法](https://shiki.style/guide/install)，使用createHighlighter、codeToHtml及inline结构输出实际静态词元，不手工着色。

## P06增量生成

P06复用同一精确依赖与主题，不改变P05输入清单。项目根目录执行：

```sh
node tools/code-highlighting/render.mjs config-p06.json
```

生成记录和原始片段分别进入执行记录/evidence/p06-20261103与artifacts/p06-code-copy/input。第24章源码阅读直接读取第23章正式源，不复制维护同一Java文件。此为静态页面生成器，不运行教学程序或测试。

## P07 输入

在本工具目录执行 `node render.mjs config-p07.json` 生成第25—29章。该清单在既有语言集上加入C语言，并明确登记第28章本地源与第29章预览源；普通与特殊构建仍分离。正文代码块按实际语言标记保留，重新生成不把Java片段改为混合日志文本。生成报告归公共P07，完整选区文本归各章evidence/browser/code-text。没有重新生成旧阶段页面。

## P08 输入

项目根目录执行 `node tools/code-highlighting/render.mjs config-p08.json`，只生成第30—32章。沿用Shiki 4.6.0、github-light与既有语言，明确加入第30章独立紧凑源文件及第32章本地C源；共7个完整源文件。报告归公共P08，原始输入和选区文本用于既有diff命令的只读核对；本阶段不重新生成历史章节。

## P09 输入

项目根执行 `node tools/code-highlighting/render.mjs config-p09.json`，仅生成第33—36章；8份完整Java阅读源含第36章独立基准与第31章明确复用源，不复制维护。沿用精确版本和主题，报告归公共P09；35节点的输入、浏览器文本和真实选区无差异，小型文本同时保留到各章browser/code-text。生成器不运行教学进程或基准，不生成测试。

## P10 全书展示整合

项目根执行 `node tools/code-highlighting/render.mjs config-p10.json`。只转换第1—12章正文、现有9个源码页、12份运行说明及六附录；观察型章不生成空源码页。旧源码页保留示例锚点与特殊变体，正文HTML仍为维护源。新增pages和readmes配置入口不改变旧阶段清单。

纯文本单行末尾的空词元统一为空行容器，复用共用样式保留真实末尾换行选区；不是向源码补字符。运行说明的Markdown表格沿用Marked渲染并包入既有table-wrap，窄屏只在局部滚动。没有新增依赖或改变主题、共用样式和教学源码。实际483节点、浏览器文本与真实选区用既有diff核对一致，详见公共P10证据。
