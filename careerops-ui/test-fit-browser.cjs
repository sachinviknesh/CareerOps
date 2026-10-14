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
    console.log('Logged in. Current URL:', page.url());

    // Click 'Fit & Details' button on the dashboard opportunity
    console.log('Clicking Fit & Details button...');
    const buttons = await page.$$('button');
    let clicked = false;
    for (const b of buttons) {
      const text = await page.evaluate(el => el.innerText, b);
      if (text.includes('Fit & Details')) {
        await b.click();
        clicked = true;
        break;
      }
    }
    console.log('Clicked Fit & Details:', clicked);

    await new Promise(r => setTimeout(r, 3000));
    console.log('Current URL on detail page:', page.url());

    await page.screenshot({ path: path.join(ARTIFACT_DIR, '04_opportunity_fit_page.png'), fullPage: true });
    console.log('Saved 04_opportunity_fit_page.png');

    const text = await page.evaluate(() => document.body.innerText);
    console.log('Contains Candidate Fit Engine:', text.includes('Candidate Fit Engine'));
    console.log('Contains Error text:', text.includes('An unexpected server error occurred'));
    console.log('Contains 43% score:', text.includes('43%'));
  } finally {
    await browser.close();
  }
}

run().catch(console.error);
