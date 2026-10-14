const puppeteer = require('puppeteer-core');
const path = require('path');
const fs = require('fs');

const EDGE_PATH = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const ARTIFACT_DIR = 'C:\\Users\\sachi\\.gemini\\antigravity-ide\\brain\\cd5e857c-0a3f-41ed-a2ed-63df68eeabef';

async function run() {
  console.log('Launching Edge from:', EDGE_PATH);
  const browser = await puppeteer.launch({
    executablePath: EDGE_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  try {
    const page = await browser.newPage();
    await page.setViewport({ width: 1280, height: 800 });

    console.log('Navigating to login page...');
    await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle0' });

    console.log('Page title:', await page.title());
    await page.screenshot({ path: path.join(ARTIFACT_DIR, '01_login_page.png') });
    console.log('Saved 01_login_page.png');

    console.log('Typing credentials...');
    await page.waitForSelector('input[name="email"], input[type="email"]');
    await page.type('input[name="email"], input[type="email"]', 'sachin6002@gmail.com');
    await page.type('input[name="password"], input[type="password"]', 'Sachin@2028');

    await page.screenshot({ path: path.join(ARTIFACT_DIR, '02_credentials_entered.png') });
    console.log('Saved 02_credentials_entered.png');

    console.log('Clicking submit button...');
    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle0', timeout: 10000 }).catch(e => console.log('Navigation event finished or timed out')),
      page.click('button[type="submit"]')
    ]);

    await new Promise(r => setTimeout(r, 2000));
    console.log('Current URL after login:', page.url());

    await page.screenshot({ path: path.join(ARTIFACT_DIR, '03_dashboard_page.png'), fullPage: true });
    console.log('Saved 03_dashboard_page.png');

    const pageContent = await page.evaluate(() => document.body.innerText);
    console.log('Dashboard text snippet:\n', pageContent.substring(0, 500));

    console.log('SUCCESS!');
  } catch (err) {
    console.error('Error during browser run:', err);
    process.exit(1);
  } finally {
    await browser.close();
  }
}

run();
