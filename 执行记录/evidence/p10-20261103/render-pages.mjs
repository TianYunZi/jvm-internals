import puppeteer from '/opt/homebrew/lib/node_modules/@mermaid-js/mermaid-cli/node_modules/puppeteer/lib/puppeteer/puppeteer.js';
import {mkdir,mkdtemp,readFile,writeFile,rm} from 'node:fs/promises';
import {join,dirname} from 'node:path';
const root='/Users/xiejinxin/Developer/Java/jvm-internals';
const config=JSON.parse(await readFile(join(root,'tools/code-highlighting/config-p10.json'),'utf8'));
const origin='http://127.0.0.1:8772';
const profile=await mkdtemp('/private/tmp/p10-publish-');
const selected=process.argv.slice(2);
const paths=selected.length?selected:[...config.pages.map(x=>x.path),...config.readmes.map(x=>`chapters/${x}/README.html`)];
const records=[];let browser;
// 仅输出出版图、正式打印及浏览器文本选区；不执行断言或自动质量判断。
const printOptions={format:'A4',printBackground:true,displayHeaderFooter:true,headerTemplate:'<div style="width:100%;font-size:8px;padding:0 17mm;text-align:right;color:#526271"><span class="title"></span></div>',footerTemplate:'<div style="width:100%;font-size:8px;text-align:center;color:#526271"><span class="pageNumber"></span> / <span class="totalPages"></span></div>'};
try{
 browser=await puppeteer.launch({executablePath:'/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',headless:true,userDataDir:profile});
 const page=await browser.newPage();
 for(const path of paths){
  const key=path.replace(/\.html$/,'');const base=join(root,'执行记录/evidence/p10-20261103/pages',key);
  await mkdir(base,{recursive:true});
  console.log('出版：'+path);
  for(const size of [{width:1440,height:1000},{width:768,height:1000},{width:390,height:844}]){
   await page.setViewport({...size,deviceScaleFactor:1});await page.goto(origin+'/'+path,{waitUntil:'networkidle0'});await page.evaluate(()=>document.fonts.ready);
   await page.screenshot({path:join(base,`screen-${size.width}.png`)});
   // 索引的宽表与旧章节代码均保留局部滚动取景，供人工阅读。
   const frame=await page.$('pre.shiki, .table-wrap, .figure-scroll');
   if(frame){await frame.evaluate(el=>{el.scrollIntoView({block:'center'});el.scrollLeft=el.scrollWidth;});await page.screenshot({path:join(base,`scroll-${size.width}.png`)});}
  }
  await page.setViewport({width:1440,height:1000,deviceScaleFactor:1});await page.goto(origin+'/'+path,{waitUntil:'networkidle0'});
  for(const figure of await page.$$('figure')){
   const id=await figure.evaluate(el=>el.id);await figure.screenshot({path:join(base,id+'.png')});
  }
  for(const node of await page.$$('code[data-code-id]')){
   const id=await node.evaluate(el=>el.dataset.codeId);
   const dom=await node.evaluate(el=>el.textContent);
   const selectedText=await node.evaluate(el=>{const range=document.createRange();range.selectNodeContents(el);const selection=getSelection();selection.removeAllRanges();selection.addRange(range);return selection.toString();});
   for(const [kind,text] of [['dom',dom],['selected',selectedText]]){
    const output=join(root,`artifacts/p10-code-copy/${kind}`,key,id+'.txt');await mkdir(dirname(output),{recursive:true});await writeFile(output,text);
   }
  }
  await page.evaluate(()=>getSelection().removeAllRanges());
  // 正文与附录全部出版；源码／完整命令选取与页型对应的最小代表。
  if(path.endsWith('chapter.html')||path.endsWith('appendix.html')||selected.length){await page.pdf({...printOptions,path:join(base,'print.pdf')});}
  records.push({page:path,viewports:['1440×1000','768×1000','390×844']});
 }
 await writeFile(join(root,'执行记录/evidence/p10-20261103',selected.length?'browser-render-revised.json':'browser-render.json'),JSON.stringify({browser:await browser.version(),origin,pages:records},null,2)+'\n');
}finally{
 // 清理仅归本次任务所有的浏览器实例与临时配置。
 if(browser)await browser.close();await rm(profile,{recursive:true,force:true});
}
