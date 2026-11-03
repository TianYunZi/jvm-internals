import puppeteer from '/opt/homebrew/lib/node_modules/@mermaid-js/mermaid-cli/node_modules/puppeteer/lib/puppeteer/puppeteer.js';
import {mkdir, mkdtemp, readFile, writeFile, rm} from 'node:fs/promises';
import {join} from 'node:path';

const root = '/Users/xiejinxin/Developer/Java/jvm-internals';
const output = join(root, '执行记录/evidence/p11-20261103');
const origin = 'http://127.0.0.1:8773';
const files = (await readFile(join(output, 'served-files.txt'), 'utf8')).trim().split('\n');
const selected = process.argv.slice(2);
const paths = selected.length ? selected : files.filter(path => path.endsWith('.html')).sort();
const revisedChapters = new Set(['ch01','ch02','ch03','ch05','ch06','ch07','ch08','ch09','ch11','ch12']);
const profile = await mkdtemp('/private/tmp/p11-publish-');
const records = [];
let browser;

// 沿用既有出版方式，只生成阅读截图、文本摘录和打印；不作断言或自动验收判定。
const printOptions = {
  format: 'A4', printBackground: true, displayHeaderFooter: true,
  headerTemplate: '<div style="width:100%;font-size:8px;padding:0 17mm;text-align:right;color:#526271"><span class="title"></span></div>',
  footerTemplate: '<div style="width:100%;font-size:8px;text-align:center;color:#526271"><span class="pageNumber"></span> / <span class="totalPages"></span></div>'
};

try {
  browser = await puppeteer.launch({
    executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
    headless: true, userDataDir: profile
  });
  const page = await browser.newPage();
  // 禁用本次独立出版实例的缓存；不修改用户浏览器或系统网络设置。
  await page.setCacheEnabled(false);
  for (const path of paths) {
    const base = join(output, 'pages', path.replace(/\.html$/, ''));
    await mkdir(base, {recursive: true});
    const revised = path.endsWith('chapter.html') && revisedChapters.has(path.split('/')[1].slice(0, 4));
    await page.setViewport({width: 1440, height: 1000, deviceScaleFactor: 1});
    await page.goto(origin + '/' + path, {waitUntil: 'load'});
    await page.evaluate(() => document.fonts.ready);
    console.log('出版页面：' + path);
    await page.screenshot({path: join(base, 'desktop.png'), fullPage: revised});
    // 保留页面当前可见正文和链接文字，供人工按实际页面逐项阅读。
    await writeFile(join(base, 'reading.txt'), await page.evaluate(() => document.body.innerText));
    await writeFile(join(base, 'links.txt'), await page.evaluate(() =>
      [...document.querySelectorAll('a')].map(a => `${a.textContent.trim()}\t${a.getAttribute('href')}`).join('\n')
    ));
    if (revised) {
      const figures = await page.$$('figure');
      for (let i = 0; i < figures.length; i++) {
        await figures[i].screenshot({path: join(base, `figure-${i + 1}.png`)});
      }
      await page.pdf({...printOptions, path: join(base, 'print.pdf')});
      for (const size of [{width: 768, height: 1000}, {width: 390, height: 844}]) {
        await page.setViewport({...size, deviceScaleFactor: 1});
        await page.goto(origin + '/' + path, {waitUntil: 'load'});
        await page.screenshot({path: join(base, `screen-${size.width}.png`)});
        const frames = await page.$$('.figure-scroll');
        for (let i = 0; i < frames.length; i++) {
          const frame = frames[i];
          await frame.evaluate(el => {el.scrollIntoView({block: 'center'}); el.scrollLeft = el.scrollWidth;});
          await page.screenshot({path: join(base, `figure-${i + 1}-${size.width}-right.png`)});
        }
      }
    }
    records.push({path, revised, screenshot: 'desktop.png'});
  }
  await writeFile(join(output, selected.length ? 'publication-revised.json' : 'publication.json'),
    JSON.stringify({browser: await browser.version(), origin, cache: 'disabled', pages: records}, null, 2) + '\n');
} catch (error) {
  console.error('本地出版失败：', error);
  throw error;
} finally {
  // 只关闭本次实例与临时配置；不影响用户已有标签和浏览器资料。
  if (browser) await browser.close();
  await rm(profile, {recursive: true, force: true});
}
