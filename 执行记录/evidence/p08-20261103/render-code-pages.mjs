import puppeteer from '/opt/homebrew/lib/node_modules/@mermaid-js/mermaid-cli/node_modules/puppeteer/lib/puppeteer/puppeteer.js';
import {mkdir,mkdtemp,readFile,writeFile,rm} from 'node:fs/promises';
import {dirname,join,relative} from 'node:path';
const root='/Users/xiejinxin/Developer/Java/jvm-internals';
const allFolders=['ch30-startup-sharing','ch31-aot-cache','ch32-native-interop'];
const folders=process.argv.length>2?process.argv.slice(2):allFolders;
const suffix=folders.length===allFolders.length?'':'-'+folders[0];
// 本脚本只渲染本地代码展示并导出可选取文本，不比较结果或作自动质量判断。
const profile=await mkdtemp('/private/tmp/p08-shiki-chrome-');let browser;
const pages=[];
try{
  browser=await puppeteer.launch({executablePath:'/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',headless:true,userDataDir:profile});
  const page=await browser.newPage();
  for(const folder of folders){
    for(const name of ['chapter.html','sources.html','README.html']){
      const file=join(root,'chapters',folder,name);const key=relative(root,file);const base=join(root,'chapters',folder,'evidence/browser');
      await page.setViewport({width:1440,height:1000,deviceScaleFactor:1});
      await page.goto('file://'+root+'/'+key,{waitUntil:'networkidle0'});await page.evaluate(()=>document.fonts.ready);
      const codeNodes=await page.$$('code[data-code-id]');
      for(const node of codeNodes){
        const id=await node.evaluate(el=>el.dataset.codeId);
        // 使用浏览器的真实文本选区取值，保留读者选择复制时的文本口径。
        const dom=await node.evaluate(el=>el.textContent);
        const domFile=join(root,'artifacts/p08-code-copy/dom',key.replace(/\.html$/,''),id+'.txt');
        await mkdir(dirname(domFile),{recursive:true});await writeFile(domFile,dom);
        const selected=await node.evaluate(el=>{const range=document.createRange();range.selectNodeContents(el);const selection=getSelection();selection.removeAllRanges();selection.addRange(range);return selection.toString();});
        const output=join(root,'artifacts/p08-code-copy/selected',key.replace(/\.html$/,''),id+'.txt');
        await mkdir(dirname(output),{recursive:true});await writeFile(output,selected);
      }
      await page.evaluate(()=>getSelection().removeAllRanges());
      if(name==='chapter.html'&&codeNodes.length){
        // 正文取景到实际代码段，分别保留桌面与窄屏可读性证据。
        for(const width of [1440,390]){
          await page.setViewport({width,height:width===390?844:1000,deviceScaleFactor:1});
          const first=await page.$('code[data-code-id]');
          await first.evaluate(el=>el.scrollIntoView({block:'center'}));
          await page.screenshot({path:join(base,`shiki-body-${width}.png`)});
        }
      }
      if(name!=='chapter.html'){
        const label=name==='sources.html'?'source':'commands';
        await page.screenshot({path:join(base,`shiki-${label}-desktop.png`)});
        await page.setViewport({width:390,height:844,deviceScaleFactor:1});
        await page.goto('file://'+root+'/'+key,{waitUntil:'networkidle0'});
        await page.screenshot({path:join(base,`shiki-${label}-mobile.png`)});
        const pre=await page.$('pre.shiki');
        if(pre){await pre.evaluate(el=>{const lines=[...el.querySelectorAll('.line')];const widest=lines.reduce((a,b)=>b.getBoundingClientRect().width>a.getBoundingClientRect().width?b:a,lines[0]);(widest??el).scrollIntoView({block:'center'});el.scrollLeft=el.scrollWidth;});await page.screenshot({path:join(base,`shiki-${label}-scroll.png`)});}
        // 选择最长的Java与命令展示作为共用代码样式的打印代表。
        if(false){
          await page.setViewport({width:1440,height:1000,deviceScaleFactor:1});
          await page.goto('file://'+root+'/'+key,{waitUntil:'networkidle0'});
          await page.pdf({path:join(base,`print/shiki-${label}.pdf`),format:'A4',printBackground:true,displayHeaderFooter:false});
        }
      }
      pages.push({page:key,codeIds:await page.$$eval('code[data-code-id]',els=>els.map(el=>el.dataset.codeId))});
      console.log('代码展示已渲染并导出选取文本：'+key);
    }
  }
  await writeFile(join(root,`执行记录/evidence/p08-20261103/shiki-browser-render${suffix}.json`),JSON.stringify({version:await browser.version(),pages},null,2)+'\n');
}finally{
  if(browser)await browser.close();await rm(profile,{recursive:true,force:true});
}
