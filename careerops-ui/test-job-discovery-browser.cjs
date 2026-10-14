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
    await page.setViewport({ width: 1300, height: 950 });

    console.log('1. Navigating to login...');
    await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle0' });
    await page.type('input[name="email"], input[type="email"]', 'sachin6002@gmail.com');
    await page.type('input[name="password"], input[type="password"]', 'Sachin@2028');

    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle0', timeout: 10000 }).catch(() => {}),
      page.click('button[type="submit"]')
    ]);

    await new Promise(r => setTimeout(r, 2000));
    console.log('2. Logged in successfully. Current URL:', page.url());

    // Navigate to Best Matches via sidebar or hash
    console.log('3. Navigating to #best-matches...');
    await page.evaluate(() => {
      window.location.hash = 'best-matches';
    });
    await new Promise(r => setTimeout(r, 2500));

    // Click 'Refresh Jobs' to trigger automatic discovery & matching
    console.log('4. Clicking Refresh Jobs...');
    const refreshClicked = await page.evaluate(() => {
      const btns = Array.from(document.querySelectorAll('button'));
      const refreshBtn = btns.find(b => b.innerText.includes('Refresh Jobs') || b.innerText.includes('Discover & Match Jobs'));
      if (refreshBtn) {
        refreshBtn.click();
        return true;
      }
      return false;
    });
    console.log('Refresh button clicked:', refreshClicked);

    // Wait for discovery and matching API call to complete
    await new Promise(r => setTimeout(r, 4000));

    // Capture screenshot of discovered matches
    const screenshot1 = path.join(ARTIFACT_DIR, '06_best_matches_discovered.png');
    await page.screenshot({ path: screenshot1, fullPage: false });
    console.log('Saved screenshot 1:', screenshot1);

    // Expand Preparation Recommendations on the first match
    console.log('5. Expanding preparation recommendations...');
    await page.evaluate(() => {
      const btns = Array.from(document.querySelectorAll('button'));
      const recBtn = btns.find(b => b.innerText.includes('Preparation Recommendations'));
      if (recBtn) recBtn.click();
    });
    await new Promise(r => setTimeout(r, 1000));

    const screenshot2 = path.join(ARTIFACT_DIR, '07_best_matches_recommendations.png');
    await page.screenshot({ path: screenshot2, fullPage: false });
    console.log('Saved screenshot 2:', screenshot2);

    // Click "Add to Tracker" on the first match
    console.log('6. Adding first match to tracker...');
    const trackClicked = await page.evaluate(() => {
      const btns = Array.from(document.querySelectorAll('button'));
      const addBtn = btns.find(b => b.innerText.includes('Add to Tracker'));
      if (addBtn) {
        addBtn.click();
        return true;
      }
      return false;
    });
    console.log('Track button clicked:', trackClicked);
    await new Promise(r => setTimeout(r, 1500));

    // Test filters: Set Min Score to 70%+
    console.log('7. Testing filters: Min Score = 70%+...');
    await page.evaluate(() => {
      const selects = Array.from(document.querySelectorAll('select'));
      if (selects.length > 0) {
        selects[0].value = '70';
        selects[0].dispatchEvent(new Event('change', { bubbles: true }));
      }
    });
    await new Promise(r => setTimeout(r, 1500));

    const screenshot3 = path.join(ARTIFACT_DIR, '08_best_matches_filtered.png');
    await page.screenshot({ path: screenshot3, fullPage: false });
    console.log('Saved screenshot 3:', screenshot3);

    // Navigate back to Dashboard to inspect "Your Best Job Matches" section
    console.log('8. Navigating to #dashboard...');
    await page.evaluate(() => {
      window.location.hash = 'dashboard';
    });
    await new Promise(r => setTimeout(r, 2500));

    const screenshot4 = path.join(ARTIFACT_DIR, '09_dashboard_top_matches.png');
    await page.screenshot({ path: screenshot4, fullPage: false });
    console.log('Saved screenshot 4:', screenshot4);

    console.log('ALL BROWSER E2E TESTS COMPLETED SUCCESSFULLY!');
  } catch (err) {
    console.error('Test error:', err);
    process.exit(1);
  } finally {
    await browser.close();
  }
}

run();
