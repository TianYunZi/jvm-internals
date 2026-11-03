import puppeteer from '/opt/homebrew/lib/node_modules/@mermaid-js/mermaid-cli/node_modules/puppeteer/lib/puppeteer/puppeteer.js';
import {mkdir, mkdtemp, writeFile, rm} from 'node:fs/promises';
import {join} from 'node:path';
import {pathToFileURL} from 'node:url';
const root='/Users/xiejinxin/Developer/Java/jvm-internals';
const allChapters=['ch13-interpreter','ch14-dispatch','ch15-dynamic-invocation','ch16-tiered-compilation','ch17-compiler-optimizations','ch18-deoptimization'];
const chapters=process.argv.length>2 ? process.argv.slice(2) : allChapters;
const suffix=chapters.length===allChapters.length ? '' : '-'+chapters[0];
// 仅把已存在的本地书稿渲染为截图和打印文件，不包含断言或自动质量判定。
const profile=await mkdtemp('/private/tmp/p05-chrome-render-');
const browser=await puppeteer.launch({executablePath:'/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',headless:true,userDataDir:profile});
const info={time:new Date().toISOString(),version:await browser.version(),profileMode:'独立临时配置；仅本地书稿',chapters:[]};
try {
  const page=await browser.newPage();
  for(const folder of chapters){
    const base=join(root,'chapters',folder);const output=join(base,'evidence/browser');
    await mkdir(join(output,'print'),{recursive:true});
    console.log('渲染章节：'+folder);
    await page.setViewport({width:1440,height:1000,deviceScaleFactor:1});
    await page.goto(pathToFileURL(join(base,'chapter.html')).href,{waitUntil:'networkidle0'});
    await page.evaluate(()=>document.fonts.ready);
    await page.screenshot({path:join(output,'headless-desktop.png')});
    // 从实际正文中的figure捕获矢量图及图注，供逐幅人工阅读。
    const figures=await page.$$('figure');
    for(const figure of figures){
      const id=await figure.evaluate(el=>el.id);
      await figure.screenshot({path:join(root,'artifacts/p05-mermaid-review',id+'-chrome.png')});
    }
    for(const size of [{width:768,height:1000},{width:390,height:844}]){
      await page.setViewport({...size,deviceScaleFactor:1});
      await page.goto(pathToFileURL(join(base,'chapter.html')).href,{waitUntil:'networkidle0'});
      await page.screenshot({path:join(output,`headless-${size.width}.png`)});
      const frame=await page.$('.figure-scroll');
      await frame.screenshot({path:join(output,`headless-figure-${size.width}-left.png`)});
      await frame.evaluate(el=>{el.scrollLeft=el.scrollWidth;});
      await frame.screenshot({path:join(output,`headless-figure-${size.width}-right.png`)});
    }
    await page.setViewport({width:1440,height:1000,deviceScaleFactor:1});
    await page.goto(pathToFileURL(join(base,'chapter.html')).href,{waitUntil:'networkidle0'});
    await page.pdf({path:join(output,'print/chapter.pdf'),format:'A4',printBackground:true,displayHeaderFooter:true,
      headerTemplate:'<div style="width:100%;font-size:8px;padding:0 17mm;text-align:right;color:#526271"><span class="title"></span></div>',
      footerTemplate:'<div style="width:100%;font-size:8px;text-align:center;color:#526271"><span class="pageNumber"></span> / <span class="totalPages"></span></div>'});
    info.chapters.push({folder,figureIds:await page.$$eval('figure',els=>els.map(el=>el.id)),paper:'A4',viewports:['1440×1000','768×1000','390×844']});
    console.log('截图与打印文件已生成：'+folder);
  }
  await writeFile(join(root,`执行记录/evidence/p05-20261103/headless-render${suffix}.json`),JSON.stringify(info,null,2)+'\n');
} finally {
  // 无论渲染是否成功，都关闭本任务离屏进程及其临时配置。
  await browser.close();
  await rm(profile,{recursive:true,force:true});
}
