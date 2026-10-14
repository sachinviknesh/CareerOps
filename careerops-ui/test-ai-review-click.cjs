const puppeteer = require('puppeteer-core');
const path = require('path');

const EDGE_PATH = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const ARTIFACT_DIR = 'C:\\Users\\sachi\\.gemini\\antigravity-ide\\brain\\cd5e857c-0a3f-41ed-a2ed-63df68eeabef';

async function run() {
  const browser = await puppeteer.launch({
    executablePath: EDGE_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  try {
    const page = await browser.newPage();
    await page.setViewport({ width: 1280, height: 900 });

    console.log('Navigating to login...');
    await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle0' });
    await page.type('input[name="email"], input[type="email"]', 'sachin6002@gmail.com');
    await page.type('input[name="password"], input[type="password"]', 'Sachin@2028');

    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle0', timeout: 10000 }).catch(() => {}),
      page.click('button[type="submit"]')
    ]);

    await new Promise(r => setTimeout(r, 2000));

    // Click 'Fit & Details' button on the dashboard opportunity
    const buttons = await page.$$('button');
    for (const b of buttons) {
      const text = await page.evaluate(el => el.innerText, b);
      if (text.includes('Fit & Details')) {
        await b.click();
        break;
      }
    }

    await new Promise(r => setTimeout(r, 2500));

    // Now click 'AI In-Depth Review'
    console.log('Clicking AI In-Depth Review button...');
    const allButtons = await page.$$('button');
    let aiBtnClicked = false;
    for (const b of allButtons) {
      const text = await page.evaluate(el => el.innerText, b);
      if (text.includes('AI In-Depth Review')) {
        await b.click();
        aiBtnClicked = true;
        break;
      }
    }
    console.log('Clicked AI button:', aiBtnClicked);

    await new Promise(r => setTimeout(r, 1500));

    await page.screenshot({ path: path.join(ARTIFACT_DIR, '05_ai_notice_banner.png'), fullPage: true });
    console.log('Saved 05_ai_notice_banner.png');

    const text = await page.evaluate(() => document.body.innerText);
    console.log('Contains Optional AI Feature notice:', text.includes('Optional AI Feature'));
  } finally {
    await browser.close();
  }
}

run().catch(console.error);
