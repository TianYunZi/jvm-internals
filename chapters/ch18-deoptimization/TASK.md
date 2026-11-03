# CH18 推测优化、去优化与代码生命周期 执行任务

## 范围与输入

P05；用户“执行P05”，对应第四篇分纲18.1.1—18.5.2、全部图解与实验主题。已读项目规则、总计划、分章清单、任务模板及本篇分纲；主环境与实现提交未漂移。只改本章，根构建、导航、公共术语由P05公共任务维护。当前执行者为本会话模型，顺序执行。

## 示例与产物

CH18-E01 RecoveryJourney.evaluate：100000次局部接收者后50000次发布接收者；CH18-E02 longLoop：单次3000000轮；均单线程、64兆二进制字节（mebibyte，MiB）堆，无计时、测试或故障耗尽。

chapter.html为唯一正式正文；sources.html为正式源码阅读副本。README.md为命令维护源，README.html为静态高亮阅读页；4图在figures，规范／实现／实测索引在evidence/索引.md，运行在evidence/runs/ch18-20261103-01。无测试、断言、故障耗尽、压力或性能基准。

## 逐节配图登记（更新规则）

按2026-11-03更新后的AGENTS.md执行。一般帧布局使用可缩放矢量图形（Scalable Vector Graphics，SVG）；流程、交互、状态和对象协作使用Mermaid，维护.mmd并本地导出.svg。混合主题按主要表达目的分类；本篇除CH13-F02帧布局外均属软件执行关系或转换。图源与导出须一起保留，原始截图仅作证据。

| 节号 | 表达目的 | 图编号 | 绘制方式与分类依据 | 文件位置 |
| --- | --- | --- | --- | --- |
| 18.1 | 优化假设失效 | F01 | 时序，Mermaid | figures/ch18-f01.mmd → .svg |
| 18.2 | 陷阱与重编译 | F01 | 复用运行时交互，Mermaid | figures/ch18-f01.mmd → .svg |
| 18.3 | 逻辑状态重建与物化 | F02 | 恢复流程，Mermaid | figures/ch18-f02.mmd → .svg |
| 18.4 | 普通入口、栈上替换及恢复方向 | F04 | 转换关系，Mermaid | figures/ch18-f04.mmd → .svg |
| 18.5 | 代码生命周期 | F03 | 状态及清理流程，Mermaid | figures/ch18-f03.mmd → .svg |

## 覆盖映射

| 要点 | 实际正文 | 示例／依据 | 状态 |
| --- | --- | --- | --- |
| 18.1.1 | chapter.html#sec-18-1-1 | E01／S01／S03 | 完成 |
| 18.1.2 | chapter.html#sec-18-1-2 | E01／S01／S03 | 完成 |
| 18.2.1 | chapter.html#sec-18-2-1 | E01／O01 | 完成 |
| 18.2.2 | chapter.html#sec-18-2-2 | E01／O01 | 完成 |
| 18.3.1 | chapter.html#sec-18-3-1 | S01 | 完成 |
| 18.3.2 | chapter.html#sec-18-3-2 | S01 | 完成 |
| 18.4.1 | chapter.html#sec-18-4-1 | E02／S02 | 完成 |
| 18.4.2 | chapter.html#sec-18-4-2 | E02／S02 | 完成 |
| 18.5.1 | chapter.html#sec-18-5-1 | S03 | 完成 |
| 18.5.2 | chapter.html#sec-18-5-2 | S03 | 完成 |
| 三项图解主题 | fig-18-1、fig-18-2、fig-18-3 | CH18-F01—F03，见各图题及依据索引 | 完成 |
| 本章实验主题 | observe；README；运行记录 | CH18-O01 | 完成，边界如下 |

## 分项状态

| 项目 | 状态 | 依据 |
| --- | --- | --- |
| 研究与来源 | 完成 | evidence/索引.md及锁定实现 |
| 源码生成 | 完成 | src/main/java与sources.html |
| 最小编译 | 完成 | compile.txt，退出0 |
| 运行与案例 | 完成 | O01；仅在已注明范围解释 |
| 正式正文 | 完成 | 十项三级要点与案例 |
| 图解与对应 | 完成 | 4幅图与图注已实际阅读，图源、导出、逐节分类及对应均已核对 |
| 浏览器阅读 | 完成 | 三视口、Shiki生成与复制、源码和命令滚动及正文8页打印通过；见evidence/browser/记录.md |
| C01—C08 | 完成 | 覆盖、解释、来源、源码、编译／观察、图文、页面与篇级衔接均有实际依据；适用边界如下 |

## 跨章边界、缺口与下一步

完整编译日志原输出为本目录compilation.xml（约7MiB）及stdout.txt（约2.1MiB），采集后原样移入项目artifacts/ch18-20261103-01；当前精选为compilation-selected.txt与stdout-selected.txt，原始文件和飞行记录摘要见input-sha256.txt。逐对象物化未直接追踪，正文明确以源码解释；未主动填满代码缓存，未实测purge时刻。两实现预先创建，不能归因新类加载。

本章C01—C08已完成。当前页面与打印依据见evidence/browser/记录.md；Shiki版本、44个片段的文本复制核对及共用代码样式阅读见[Shiki验收](../../执行记录/evidence/p05-20261103/Shiki验收.md)，篇级出口见[完成验收](../../执行记录/evidence/p05-20261103/完成验收.md)。后续按上述跨章职责承接，不重跑当前充分的编译与观察。更新时间：2026-11-03，Asia/Shanghai。
