const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');
const readline = require('readline');

const COOKIES_PATH = path.join(__dirname, 'jira-cookies.json');
const JIRA_URL = 'https://mhclgdigital.atlassian.net/';

async function waitForEnter(prompt) {
  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
  });
  return new Promise(resolve => {
    rl.question(prompt, () => {
      rl.close();
      resolve();
    });
  });
}

async function authenticate() {
  const waitMinutes = parseInt(process.argv[3]) || 5;
  
  console.log('Launching browser...');
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext();
  const page = await context.newPage();
  
  console.log('Navigating to Jira...');
  await page.goto(JIRA_URL, { waitUntil: 'domcontentloaded' });
  
  console.log('');
  console.log('===========================================');
  console.log(`You have ${waitMinutes} minutes to authenticate.`);
  console.log('The browser will stay open during this time.');
  console.log('===========================================');
  console.log('');
  
  // Wait for the specified time
  for (let i = waitMinutes * 60; i > 0; i--) {
    process.stdout.write(`\rTime remaining: ${Math.floor(i/60)}:${(i%60).toString().padStart(2,'0')} `);
    await new Promise(r => setTimeout(r, 1000));
  }
  console.log('');
  
  console.log('Extracting cookies...');
  const cookies = await context.cookies();
  fs.writeFileSync(COOKIES_PATH, JSON.stringify(cookies, null, 2));
  console.log(`Saved ${cookies.length} cookies to ${COOKIES_PATH}`);
  
  await browser.close();
  console.log('Done.');
}

async function openWithCookies(ticketUrl) {
  if (!fs.existsSync(COOKIES_PATH)) {
    console.error('No cookies found. Run with --auth first.');
    process.exit(1);
  }
  
  const cookies = JSON.parse(fs.readFileSync(COOKIES_PATH, 'utf-8'));
  const headless = process.argv.includes('--headless');
  
  const browser = await chromium.launch({ headless });
  const context = await browser.newContext();
  await context.addCookies(cookies);
  
  const page = await context.newPage();
  await page.goto(ticketUrl || JIRA_URL, { waitUntil: 'domcontentloaded', timeout: 60000 });
  
  // Check if we're still authenticated
  const url = page.url();
  
  if (url.includes('login') || url.includes('auth')) {
    console.error('SESSION_EXPIRED: Re-run with --auth to re-authenticate.');
    await browser.close();
    process.exit(2);
  }
  
  // Wait for main content to load
  await page.waitForTimeout(5000);
  
  // Extract ticket content
  const text = await page.evaluate(() => {
    // Try to get the main ticket content area
    const mainContent = document.querySelector('[data-testid="issue.views.issue-base.foundation.summary.heading"]')?.innerText || '';
    const description = document.querySelector('[data-testid="issue.views.field.rich-text.description"]')?.innerText || '';
    const details = document.querySelector('[data-testid="issue.views.issue-base.context.context-group"]')?.innerText || '';
    const comments = document.querySelector('[data-testid="issue-activity-feed"]')?.innerText || '';
    
    // If specific selectors fail, fall back to body text
    if (!mainContent && !description) {
      return document.body.innerText;
    }
    
    return `SUMMARY:\n${mainContent}\n\nDESCRIPTION:\n${description}\n\nDETAILS:\n${details}\n\nCOMMENTS:\n${comments}`;
  });
  
  console.log(text);
  
  // Save refreshed cookies
  const newCookies = await context.cookies();
  fs.writeFileSync(COOKIES_PATH, JSON.stringify(newCookies, null, 2));
  
  await browser.close();
}

const args = process.argv.slice(2);
if (args[0] === '--auth') {
  authenticate();
} else {
  openWithCookies(args[0]);
}
