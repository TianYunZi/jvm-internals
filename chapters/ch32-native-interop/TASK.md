# CH32 本地调用与外部内存

- 用户授权：2026-11-03“执行P08”。本章目录为修改范围；公共配置、入口与篇级变更另见公共任务。
- 输入：总纲对应节、执行总计划、分章清单、第七篇分纲、公共环境基线和本章所需前置。
- 环境：Java开发工具包（Java Development Kit，JDK）25.0.5.0.2，OpenJDK提交1775ea114f649f0314e4e43eb6c802ff16dfab7c；macOS AArch64。
- 术语：类数据共享（Class Data Sharing，CDS）、提前处理（Ahead-of-Time，AOT）、Java本地接口（Java Native Interface，JNI）、外部函数与内存（Foreign Function & Memory，FFM）、应用二进制接口（Application Binary Interface，ABI）、可缩放矢量图形（Scalable Vector Graphics，SVG）。路径及函数标识保留原名。
- 授权分类：教学源码与正常有界观察随阶段实施；没有测试、断言、故障复现、压力或性能基准代码。危险终止及本地越界只作来源分析。
- 示例：CH32-E01 NativeJourney与interop.c；E02 ForeignJourney与同一本地库。普通Java与显式本地构建分开。

## 大纲覆盖

| 三级要点 | 正文锚点 | 对应材料 |
| --- | --- | --- |
| 32.1.1 | [正文](chapter.html#sec-32-1-1) | 本节因果解释、来源与[观察边界](chapter.html#observe)；图与源按下表及证据索引对应 |
| 32.1.2 | [正文](chapter.html#sec-32-1-2) | 本节因果解释、来源与[观察边界](chapter.html#observe)；图与源按下表及证据索引对应 |
| 32.2.1 | [正文](chapter.html#sec-32-2-1) | 本节因果解释、来源与[观察边界](chapter.html#observe)；图与源按下表及证据索引对应 |
| 32.2.2 | [正文](chapter.html#sec-32-2-2) | 本节因果解释、来源与[观察边界](chapter.html#observe)；图与源按下表及证据索引对应 |
| 32.3.1 | [正文](chapter.html#sec-32-3-1) | 本节因果解释、来源与[观察边界](chapter.html#observe)；图与源按下表及证据索引对应 |
| 32.3.2 | [正文](chapter.html#sec-32-3-2) | 本节因果解释、来源与[观察边界](chapter.html#observe)；图与源按下表及证据索引对应 |
| 32.4.1 | [正文](chapter.html#sec-32-4-1) | 本节因果解释、来源与[观察边界](chapter.html#observe)；图与源按下表及证据索引对应 |
| 32.4.2 | [正文](chapter.html#sec-32-4-2) | 本节因果解释、来源与[观察边界](chapter.html#observe)；图与源按下表及证据索引对应 |
| 32.5.1 | [正文](chapter.html#sec-32-5-1) | 本节因果解释、来源与[观察边界](chapter.html#observe)；图与源按下表及证据索引对应 |
| 32.5.2 | [正文](chapter.html#sec-32-5-2) | 本节因果解释、来源与[观察边界](chapter.html#observe)；图与源按下表及证据索引对应 |
| 32.6.1 | [正文](chapter.html#sec-32-6-1) | 本节因果解释、来源与[观察边界](chapter.html#observe)；图与源按下表及证据索引对应 |
| 32.6.2 | [正文](chapter.html#sec-32-6-2) | 本节因果解释、来源与[观察边界](chapter.html#observe)；图与源按下表及证据索引对应 |

## 逐节配图登记

| 图号 | 对应节 | 绘制方式与表达目的 | 图源与显示图 | 正文 |
| --- | --- | --- | --- | --- |
| CH32-F01 | 32.1 | Mermaid：状态转换、外部线程与回调 | [图源](figures/ch32-f01.mmd) → [显示图](figures/ch32-f01.svg) | [图32-1](chapter.html#fig-32-1) |
| CH32-F02 | 32.2 | Mermaid：引用、句柄与可达性 | [图源](figures/ch32-f02.mmd) → [显示图](figures/ch32-f02.svg) | [图32-2](chapter.html#fig-32-2) |
| CH32-F03 | 32.3 | Mermaid：短数组临界区与固定协议 | [图源](figures/ch32-f03.mmd) → [显示图](figures/ch32-f03.svg) | [图32-3](chapter.html#fig-32-3) |
| CH32-F04 | 32.4 | SVG：内存段空间与时间边界 | [可编辑矢量源及显示图](figures/ch32-f04.svg) | [图32-4](chapter.html#fig-32-4) |
| CH32-F05 | 32.5—32.6 | Mermaid：调用、回调、库与Arena有效期 | [图源](figures/ch32-f05.mmd) → [显示图](figures/ch32-f05.svg) | [图32-5](chapter.html#fig-32-5) |

软件关系、交互与流程用Mermaid；空间和概念时间线用SVG。时间线没有实测刻度，图中切片为明确标注的机制示意。图源修订后已重新导出，没有以位图封装代替绘图。

## 分项状态与C01—C08

| 项目 | 状态与证据 |
| --- | --- |
| 研究／C03来源 | 完成；[证据索引](evidence/索引.md)、锁定符号、精选实现原文与源文件摘要；官方命令／规范及适用类库文档已读取，网页403未冒充已读 |
| 正文／C01覆盖 | 完成；本章12个三级要点均有对应实质解释、示例、机制及边界 |
| 原理／C02因果 | 完成；规范、锁定实现与有限观测分别说明，不以成功运行替代语义 |
| 源码及编译／C04 | 完成；正式源和运行说明齐备，公共仅3普通模块聚合编译退出0；紧凑源与本地构建按本章说明独立处理 |
| 运行／C05 | 完成；[本轮记录](evidence/runs/ch32-20261103-01/记录.md)及实际输出、源与产物摘要；未触发路径明确说明 |
| 图解／C06 | 完成；5图的图源、显示图、分类与图注对应，最终屏幕及打印实读 |
| 页面／C07 | 完成；Shiki4.6.0静态输出；三视口、代码选区及本章9页正文打印逐页实读；[浏览器记录](evidence/browser/记录.md) |
| 整体／C08 | 完成；与CH07初始化、CH16／18画像、CH12所有权、CH20回收协作及CH28本地固定边界一致，见公共篇级核对 |

## 当前交接

本章C01—C08达到本轮适用范围；源码、正文、图解、编译、正常观察、静态高亮及真实阅读分别有记录。未决／限制：只有macOS AArch64本地调用实测；未观察弱引用清除，未执行错误线程、释放后访问、越界、泄漏或崩溃；源码中错误清理分支未触发。

下一步：P08出口见[完成验收](../../执行记录/evidence/p08-20261103/完成验收.md)。本章不启动P09；全书迁移、历史版本及综合性能案例继续归对应后续任务。更新：2026-11-03，Asia/Shanghai。
