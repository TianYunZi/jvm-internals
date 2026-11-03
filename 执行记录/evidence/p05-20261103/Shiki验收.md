# P05 静态代码展示验收

2026-11-03，Asia/Shanghai。按更新后的项目规则，P05六章正文、源码阅读及运行说明的代码统一由Shiki生成静态超文本标记语言（Hypertext Markup Language，HTML）。本轮只处理P05，未追溯转换其他章节。

## 固定输入与生成

Shiki 4.6.0、github-light主题，Marked 17.0.5；精确版本与传递依赖在tools/code-highlighting/package-lock.json。加载java、bash、xml、json、cpp、asm，混合日志、字节码和路径明确使用text。实际安装见shiki-installed.txt。实现使用createHighlighter与codeToHtml，行内采用inline结构；[官方用法](https://shiki.style/guide/install)已核查，实际输出包含Shiki生成的颜色词元，未手工着色。

生成入口是tools/code-highlighting/render.mjs，配置config.json，命令npm run generate:p05。六个sources.html直接读取正式Java文件；chapter.html从原代码节点恢复文本后高亮；README.md的围栏命令按bash生成README.html。围栏之间的原始末尾换行一并保留。正文链接已指向本地运行说明页面，读者无需安装作者依赖或运行脚本。实际44个展示节点及语言、源路径、文本摘要见shiki-generation.json，运行输出见shiki-generation.txt。

## 原文与实际复制

用户已授权独立无界面Chrome仅渲染本地P05书稿。本次使用独立临时配置的Chrome156.0.8078.80，以file协议直接打开全部18页，导出每个代码节点的页面文本及浏览器真实选区字符串；导出器仅生成截图、打印和文本，不比较结果、不含断言或自动质量判定。记录见render-code-pages.mjs、shiki-browser-render.json与同名文本日志。

输入片段、页面文本、实际选区文本分别在artifacts/p05-code-copy/input、dom、selected。使用既有diff -r分别比较input与dom、input与selected，两次均退出0，结果文件shiki-dom-diff.txt、shiki-copy-diff.txt均为空。六个完整Java选区的安全散列算法256位（Secure Hash Algorithm 256-bit，SHA-256）与正式.java逐一相同，见shiki-source-copy-sha256.txt；它们也与本轮运行输入摘要一致。

最初浏览器选取会丢失最后一个换行，失败差异保留在shiki-copy-diff-before-eol.txt。共用样式为Shiki最后一个空行保留零宽、非零高度的选取位置，没有向代码文本添加字符；修正后全部44个选区含缩进、特殊字符及末尾换行均与输入相同。没有用移除末尾换行或规范化空白来掩盖差异。

## 实际阅读范围与结果

- 六章源码页与运行说明页的1440×1000、390×844截图均已实读；六份Java及六组长命令均查看局部横移后的最长行右端，文字清晰，滚动限于代码框。代码、路径和中文注释保持真实文本，长路径标题允许换行。
- 有代码展示的正文CH13、CH15、CH17另取1440与390宽度的实际代码段截图并实读；长实现符号仍可在手机内换行，行内颜色不遮挡文字。正文三视口与图文阅读另见打印视觉验收.md。
- 最长Java源CH15的完整3页、最长命令说明CH17的完整3页实际打印已逐页实读：词元对比度、中文注释、缩进、长行折行和跨页续接可读，无横向裁切。两份代表分别位于章节evidence/browser/print/shiki-source.pdf与shiki-commands.pdf；没有声称所有支持页面都各自独立打印验收。
- 六章正式正文重新生成48页打印。Shiki变更前后同尺寸页图用既有diff比较：11页不同，均已逐页重新实读；其余37页图文件逐字节一致，承接此前实际阅读。CH17末页也已补读。具体页号与当前结果见打印视觉验收.md，未用文字抽取代替图文阅读。
- 实际浏览器重载后点击CH16正文→README.html→sources.html→正文成功；既有12—18章导航、全书目录与其他源码入口的实读记录保留。18个页面以file协议加载本地样式／图像完成渲染，不加载Shiki脚本或远程字体。

截图位于各章evidence/browser/shiki-*.png；逐页打印图、拼接阅读图在artifacts/p05-shiki-review。两种独立作者渲染器退出时均关闭浏览器并移除自身临时配置，不使用用户浏览器配置。高亮与页面通过不替代Java编译或机制证据；本轮没有修改Java源码或新增测试。
