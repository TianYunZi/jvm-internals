import {createHighlighter} from 'shiki';
import {Marked, Renderer} from 'marked';
import {createHash} from 'node:crypto';
import {readFile, writeFile, readdir, mkdir} from 'node:fs/promises';
import {dirname, join, relative} from 'node:path';
import {fileURLToPath} from 'node:url';

const toolDir=dirname(fileURLToPath(import.meta.url));
const root=join(toolDir,'../..');
// 各阶段选择自己的输入清单，保留既有P05生成范围和历史记录。
const config=JSON.parse(await readFile(join(toolDir,process.argv[2]??'config.json'),'utf8'));
const stage=config.stage??'p05';
const highlighter=await createHighlighter({themes:[config.theme],langs:config.languages});
const records=[];
const inputs=[];
let pageKey='';
let pageSequence=0;

/** HTML只承担展示；解码一次即可恢复既有代码节点中的原始字符。 */
function decodeCode(html){
  const entities={amp:'&',lt:'<',gt:'>',quot:'"',apos:"'"};
  return html.replace(/<br\s*\/?\s*>/gi,'\n').replace(/<[^>]*>/g,'')
    .replace(/&#(x[0-9a-f]+|\d+);|&(amp|lt|gt|quot|apos);/gi,(_,number,name)=>number
      ?String.fromCodePoint(Number.parseInt(number[0].toLowerCase()==='x'?number.slice(1):number,number[0].toLowerCase()==='x'?16:10))
      :entities[name.toLowerCase()]);
}

/** 属性和源码路径使用普通文本转义，不把生成器输入当作网页结构。 */
function escapeHtml(text){
  return text.replace(/[&<>"']/g,ch=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[ch]));
}

/** 每个片段留存真实输入，供浏览器选取文本的只读差异核对使用。 */
function register(code,lang,kind,sourceFile){
  const id='code-'+String(++pageSequence).padStart(3,'0');
  const digest=createHash('sha256').update(code).digest('hex');
  const entry={page:pageKey,id,language:lang,kind,sourceFile,sha256:digest};
  records.push(entry);
  inputs.push({...entry,code});
  return id;
}

/** 终端和配置按真实语言；混合日志及不具备专用语法的字节码采用text。 */
function normalizeLanguage(language){
  const aliases={sh:'bash',shell:'bash',shellscript:'bash',js:'javascript',plaintext:'text',txt:'text'};
  return aliases[language]??language??'text';
}

function block(code,lang,sourceFile){
  const id=register(code,lang,'block',sourceFile);
  let html=highlighter.codeToHtml(code,{lang,theme:config.theme});
  // 纯文本单行末尾的空词元会包一层空span；统一为空行容器以保留真实换行选区。
  if(lang==='text'&&/^[^\n]*\n$/.test(code)){
    html=html.replace('<span class="line"><span></span></span>','<span class="line"></span>');
  }
  return html
    .replace('<pre ',`<pre data-shiki-version="${config.shikiVersion}" `)
    .replace('<code>',`<code data-code-id="${id}" data-code-language="${lang}"${sourceFile?` data-source-file="${escapeHtml(sourceFile)}"`:''}>`);
}

function inline(code,lang='text'){
  const id=register(code,lang,'inline');
  const tokens=highlighter.codeToHtml(code,{lang,theme:config.theme,structure:'inline'});
  return `<code class="shiki-inline" data-shiki-version="${config.shikiVersion}" data-code-id="${id}" data-code-language="${lang}">${tokens}</code>`;
}

/** 短符号仍是代码片段；明确区分汇编、源语言符号和纯文本字节码。 */
function inlineLanguage(code,folder){
  if(/^(addw?|addl|eaddl|ldr|str)\s/.test(code))return 'asm';
  if(folder==='ch15-dynamic-invocation')return 'java';
  if(code.includes('::')&&folder!=='ch15-dynamic-invocation')return 'cpp';
  // 新篇中的Java调用与声明按源语言处理，参数名等纯文本仍不伪装成代码。
  if(/^ch(?:19|2[0-4])-/.test(folder)&&/^(?:Reference\.|PhantomReference\.|Cleaner$|System\.|volatile\s|finalize$|clean\(\)$)/.test(code))return 'java';
  return 'text';
}

/** 原正文作为维护源；暂存块节点，避免把Shiki自己的code节点重复嵌套。 */
function chapterHtml(html,folder){
  const blocks=[];
  let output=html.replace(/<pre\b[^>]*>\s*<code\b([^>]*)>([\s\S]*?)<\/code>\s*<\/pre>/gi,(_,attributes,code)=>{
    // 原片段的语言标记在重新生成时保留，未标记者仍按混合文本处理。
    const language=attributes.match(/data-code-language="([^"]+)"|language-([\w-]+)/);
    const lang=normalizeLanguage(language?.[1]??language?.[2]??'text');
    const index=blocks.push(block(decodeCode(code),lang))-1;
    return `<!--P05-SHIKI-BLOCK-${index}-->`;
  });
  output=output.replace(/<code\b[^>]*>([\s\S]*?)<\/code>/gi,(_,code)=>{
    const text=decodeCode(code);return inline(text,inlineLanguage(text,folder));
  });
  output=output.replace(/<!--P05-SHIKI-BLOCK-(\d+)-->/g,(_,index)=>blocks[Number(index)]);
  // 静态正文继续使用本地运行说明，命令也由同一高亮器输出。
  return output.replaceAll('href="README.md"','href="README.html"');
}

/** 源码阅读副本始终从正式Java文件生成，不以既有展示HTML反推源文件。 */
async function sourcePage(folder){
  const base=join(root,'chapters',folder);
  const sourceDir=join(root,'chapters',config.sourceFolders?.[folder]??folder,'src/main/java');
  const pieces=[];
  async function visit(dir){
    const children=await readdir(dir,{withFileTypes:true});
    children.sort((a,b)=>a.name.localeCompare(b.name));
    for(const child of children){
      const path=join(dir,child.name);
      if(child.isDirectory())await visit(path);
      else if(child.name.endsWith('.java')){
        const sourceFile=relative(root,path);const title=relative(base,path);
        pieces.push(`<h2>${inline(title,'text')}</h2>`+block(await readFile(path,'utf8'),'java',sourceFile));
      }
    }
  }
  await visit(sourceDir);
  // 预览Java与本地C源属于独立构建；仅在明确清单中加入阅读页。
  for(const extra of config.extraSourceFiles?.[folder]??[]){
    const path=join(root,extra.path);
    pieces.push(`<h2>${inline(relative(base,path),'text')}</h2>`+block(await readFile(path,'utf8'),extra.language,extra.path));
  }
  return document(folder,'源码阅读',`<p>由正式源文件生成的静态阅读副本。<a href="chapter.html">返回正文</a> · <a href="README.html">运行说明</a></p>`+pieces.join('\n'));
}

function document(folder,label,content){
  const n=Number(folder.slice(2,4));
  return `<!doctype html><html lang="zh-CN"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="shiki-version" content="${config.shikiVersion}"><title>第${n}章${label}</title><link rel="stylesheet" href="../../assets/book.css"></head><body><main class="home" id="main"><h1>第${n}章${label}</h1>${content}</main></body></html>`;
}

try{
  // 整合既有源码页时保留原锚点及模块变体；不按普通源码目录重建页面。
  for(const entry of config.pages??[]){
    console.log('保留结构的静态高亮：'+entry.path);
    pageKey=entry.path;pageSequence=0;
    const file=join(root,entry.path);
    await writeFile(file,chapterHtml(await readFile(file,'utf8'),entry.folder??''));
  }
  // 观察型章节没有自己的源码页，运行说明中的复用入口由原文保留。
  for(const folder of config.readmes??[]){
    console.log('运行说明静态高亮：'+folder);
    pageKey=`chapters/${folder}/README.html`;pageSequence=0;
    const renderer=new Marked({renderer:{
      // Markdown宽表与正文表格使用相同局部滚动容器，避免窄屏撑宽整页。
      table(token){return '<div class="table-wrap">'+Renderer.prototype.table.call(this,token)+'</div>';},
      code:({text,lang,raw})=>{
        const firstLine=raw.indexOf('\n');const closing=raw.lastIndexOf('```');
        const original=raw.startsWith('```')&&closing>firstLine?raw.slice(firstLine+1,closing):text;
        return block(original,normalizeLanguage(lang?.split(/\s/)[0]||'text'));
      },
      codespan:({text})=>inline(text,'text')
    }});
    const content=renderer.parse(await readFile(join(root,'chapters',folder,'README.md'),'utf8'));
    await writeFile(join(root,pageKey),document(folder,'运行说明',`<p><a href="chapter.html">返回正文</a></p>${content.replace(/^<h1[^>]*>[\s\S]*?<\/h1>\s*/, '')}`));
  }
  for(const folder of config.chapters){
    console.log('静态高亮：'+folder);
    const base=join(root,'chapters',folder);
    pageKey=relative(root,join(base,'chapter.html'));pageSequence=0;
    await writeFile(join(base,'chapter.html'),chapterHtml(await readFile(join(base,'chapter.html'),'utf8'),folder));
    pageKey=relative(root,join(base,'sources.html'));pageSequence=0;
    await writeFile(join(base,'sources.html'),await sourcePage(folder));
    pageKey=relative(root,join(base,'README.html'));pageSequence=0;
    const renderer=new Marked({renderer:{
      code:({text,lang,raw})=>{
        // 围栏边界不属于代码；保留边界之间的全部缩进与末尾换行。
        const firstLine=raw.indexOf('\n');const closing=raw.lastIndexOf('```');
        const original=raw.startsWith('```')&&closing>firstLine ? raw.slice(firstLine+1,closing) : text;
        return block(original,normalizeLanguage(lang?.split(/\s/)[0]||'text'));
      },
      codespan:({text})=>inline(text,'text')
    }});
    const readme=renderer.parse(await readFile(join(base,'README.md'),'utf8'));
    await writeFile(join(base,'README.html'),document(folder,'运行说明',`<p><a href="chapter.html">返回正文</a> · <a href="sources.html">源码阅读</a></p>${readme.replace(/^<h1[^>]*>[\s\S]*?<\/h1>\s*/, '')}`));
  }
  const reportDir=join(root,`执行记录/evidence/${stage}-20261103`);
  await writeFile(join(reportDir,'shiki-generation.json'),JSON.stringify({version:config.shikiVersion,theme:config.theme,languages:config.languages,plainTextLanguage:config.plainTextLanguage,records},null,2)+'\n');
  const copyRoot=join(root,`artifacts/${stage}-code-copy/input`);
  for(const input of inputs){
    const output=join(copyRoot,input.page.replace(/\.html$/,''),input.id+'.txt');
    await mkdir(dirname(output),{recursive:true});await writeFile(output,input.code);
  }
  console.log('生成完成：'+records.length+'个代码展示节点；离线页面不加载Shiki脚本。');
}finally{
  highlighter.dispose();
}
