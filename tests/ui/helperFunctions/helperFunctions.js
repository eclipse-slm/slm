const { chromium } = require('playwright');
const { cwd } = require('process');

// Adjust url to match your needs
async function loginHelper(page) {
    await page.goto('http://localhost:8080', {
        waitUntil: 'domcontentloaded'
    });
    await page.getByLabel('Username or email').fill('fabos');
    await page.getByLabel('Password').fill('password');
    await page.getByRole('button', { name: 'Sign In' }).click();
    await page.waitForURL('**/#/**');
    return true;
}

function delay(time) {
    return new Promise(function (resolve) {
        setTimeout(resolve, time);
    });
}

const CURRENT_DIR = cwd();

module.exports = { loginHelper, delay, CURRENT_DIR };
