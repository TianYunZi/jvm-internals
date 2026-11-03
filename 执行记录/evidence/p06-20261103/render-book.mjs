import puppeteer from '/opt/homebrew/lib/node_modules/@mermaid-js/mermaid-cli/node_modules/puppeteer/lib/puppeteer/puppeteer.js';
import {mkdir, mkdtemp, writeFile, rm} from 'node:fs/promises';
import {join} from 'node:path';
const root='/Users/xiejinxin/Developer/Java/jvm-internals';
const allChapters=['ch19-gc-foundations','ch20-safepoints-barriers','ch21-basic-collectors','ch22-g1','ch23-zgc','ch24-shenandoah'];
const chapters=process.argv.length>2?process.argv.slice(2):allChapters;
const reportSuffix=chapters.length===allChapters.length?'':'-revised';
const origin='http://127.0.0.1:8768';
// 用户已授权本机文稿渲染；本工具仅输出截图和打印文件，不作断言或自动质量判断。
const profile=await mkdtemp('/private/tmp/p06-chrome-render-');
const browser=await puppeteer.launch({executablePath:'/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',headless:true,userDataDir:profile});
const info={time:new Date().toISOString(),version:await browser.version(),origin,profileMode:'独立临时配置；仅访问本机书稿',chapters:[]};
const printOptions={format:'A4',printBackground:true,displayHeaderFooter:true,
  headerTemplate:'<div style="width:100%;font-size:8px;padding:0 17mm;text-align:right;color:#526271"><span class="title"></span></div>',
  footerTemplate:'<div style="width:100%;font-size:8px;text-align:center;color:#526271"><span class="pageNumber"></span> / <span class="totalPages"></span></div>'};
try {
  const page=await browser.newPage();
  for(const folder of chapters){
    const output=join(root,'chapters',folder,'evidence/browser');
    await mkdir(join(output,'print'),{recursive:true});
    console.log('渲染章节：'+folder);
    await page.setViewport({width:1440,height:1000,deviceScaleFactor:1});
    await page.goto(`${origin}/chapters/${folder}/chapter.html`,{waitUntil:'networkidle0'});
    await page.evaluate(()=>document.fonts.ready);
    await page.screenshot({path:join(output,'desktop.png')});
    for(const figure of await page.$$('figure')){
      const id=await figure.evaluate(el=>el.id);
      await figure.screenshot({path:join(root,'artifacts/p06-mermaid-review',id+'.png')});
    }
    for(const size of [{width:768,height:1000},{width:390,height:844}]){
      await page.setViewport({...size,deviceScaleFactor:1});
      await page.goto(`${origin}/chapters/${folder}/chapter.html`,{waitUntil:'networkidle0'});
      await page.screenshot({path:join(output,`screen-${size.width}.png`)});
      const frame=await page.$('.figure-scroll');
      await frame.screenshot({path:join(output,`figure-${size.width}-left.png`)});
      await frame.evaluate(el=>{el.scrollLeft=el.scrollWidth;});
      await frame.screenshot({path:join(output,`figure-${size.width}-right.png`)});
    }
    await page.setViewport({width:1440,height:1000,deviceScaleFactor:1});
    await page.goto(`${origin}/chapters/${folder}/chapter.html`,{waitUntil:'networkidle0'});
    await page.pdf({...printOptions,path:join(output,'print/chapter.pdf')});
    for(const name of ['sources','README']){
      await page.goto(`${origin}/chapters/${folder}/${name}.html`,{waitUntil:'networkidle0'});
      await page.screenshot({path:join(output,`${name}-desktop.png`)});
      if((folder==='ch20-safepoints-barriers'&&name==='sources')||(folder==='ch21-basic-collectors'&&name==='README')){
        await page.pdf({...printOptions,path:join(output,`print/${name}.pdf`)});
      }
      await page.setViewport({width:390,height:844,deviceScaleFactor:1});
      await page.screenshot({path:join(output,`${name}-390.png`)});
      const code=await page.$('pre.shiki');
      await code.evaluate(el=>{el.scrollLeft=el.scrollWidth;});
      await code.screenshot({path:join(output,`${name}-code-390-right.png`)});
      await page.setViewport({width:1440,height:1000,deviceScaleFactor:1});
    }
    info.chapters.push({folder,paper:'A4',viewports:['1440×1000','768×1000','390×844']});
    console.log('本章截图与打印完成：'+folder);
  }
  await writeFile(join(root,`执行记录/evidence/p06-20261103/browser-render${reportSuffix}.json`),JSON.stringify(info,null,2)+'\n');
} finally {
  // 正常或异常都只关闭本任务独立实例，并清理本任务临时配置。
  await browser.close();
  await rm(profile,{recursive:true,force:true});
}
