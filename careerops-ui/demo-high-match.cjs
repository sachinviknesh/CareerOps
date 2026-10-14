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

    console.log('Navigating to login...');
    await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle0' });
    await page.type('input[name="email"], input[type="email"]', 'sachin6002@gmail.com');
    await page.type('input[name="password"], input[type="password"]', 'Sachin@2028');

    await Promise.all([
      page.waitForNavigation({ waitUntil: 'networkidle0', timeout: 10000 }).catch(() => {}),
      page.click('button[type="submit"]')
    ]);

    await new Promise(r => setTimeout(r, 2000));

    // Update profile via API in browser context to have SOC Analyst target role and relevant cybersecurity skills
    console.log('Enriching candidate profile with target role and skills via API...');
    const result = await page.evaluate(async () => {
      const token = localStorage.getItem('careerops_token');
      // 1. Get profile
      const profRes = await fetch('/api/candidates/me', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const prof = await profRes.json();

      // 2. Get available skills
      const skillsRes = await fetch('/api/skills', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const allSkills = await skillsRes.json();

      // Pick SIEM, Linux, Incident Response, Network Security, Python, Java, Docker
      const targetSkillNames = ['siem', 'linux', 'incident response', 'network security', 'python', 'java', 'docker', 'wireshark'];
      const selectedSkillIds = allSkills
        .filter(s => targetSkillNames.some(t => s.name.toLowerCase().includes(t)))
        .map(s => s.id);

      // 3. Update profile
      const updateRes = await fetch('/api/candidates/me', {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          name: prof.name || 'Sachin',
          email: prof.email,
          headline: 'Cybersecurity Analyst & SOC Operations Specialist',
          location: 'San Francisco, CA',
          education: 'B.S. in Computer Science & Cybersecurity',
          experienceYears: 2,
          targetRoles: 'SOC Analyst, Security Engineer, Incident Responder',
          resumeText: 'Cybersecurity analyst with hands-on SIEM monitoring, Wireshark packet analysis, Linux systems administration, network security protocols, Python automation scripts, and incident response triage.',
          skillIds: selectedSkillIds
        })
      });

      // 4. Recalculate matches
      const matchRes = await fetch('/api/matches/recalculate', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const matches = await matchRes.json();
      return { matchesCount: matches.length, topScore: matches[0]?.fitScore, topTitle: matches[0]?.title };
    });

    console.log('Profile enriched and matches recalculated:', result);

    // Refresh page at #best-matches
    await page.goto('http://localhost:5173/#best-matches', { waitUntil: 'networkidle0' });
    await new Promise(r => setTimeout(r, 2000));

    const screenshot1 = path.join(ARTIFACT_DIR, '10_best_matches_high_fit.png');
    await page.screenshot({ path: screenshot1, fullPage: false });
    console.log('Saved screenshot:', screenshot1);

    // Navigate to dashboard
    await page.goto('http://localhost:5173/#dashboard', { waitUntil: 'networkidle0' });
    await new Promise(r => setTimeout(r, 2000));

    const screenshot2 = path.join(ARTIFACT_DIR, '11_dashboard_high_fit_top_matches.png');
    await page.screenshot({ path: screenshot2, fullPage: false });
    console.log('Saved screenshot:', screenshot2);

  } catch (err) {
    console.error('Error:', err);
  } finally {
    await browser.close();
  }
}

run();
