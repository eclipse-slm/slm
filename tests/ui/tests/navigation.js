import { test, expect } from '@playwright/test';
import { loginHelper, CURRENT_DIR } from '../helperFunctions/helperFunctions';

// Testing the navigation between pages and to get used to Playwright.
test.describe('navigation tests', () => {
    test.beforeEach(async ({ page }) => {
        // Go to FabOS login page.
        await page.goto('http://localhost:8080');
        await page.getByLabel('Username or email').click();
        await page.getByLabel('Username or email').fill('fabos');
        await page.getByLabel('Password').click();
        await page.getByLabel('Password').fill('password');
        await page.getByRole('button', { name: 'Sign In' }).click();
    });

    test('navigate to dashboard', async ({ page }) => {
        // Go to page 'Dashboard'.
        await page.getByRole('link', { name: 'Jobs' }).click();
        await page.waitForURL('**/#/jobs');
        await page.getByRole('link', { name: 'Dashboard' }).click();
        await page.waitForURL('**/#/');
        // Take screenshot
        await page.screenshot({ path: './../screenshots/dashboard_page.png' });

        // Expects the page 'Dashboard' to be displayed.
        await expect(page.url().includes('#')).toBeTruthy();
    });

    test('navigate to jobs', async ({ page }) => {
        // Go to page 'Jobs'.
        await page.getByRole('link', { name: 'Jobs' }).click();
        await page.waitForURL('**/#/jobs');

        // Expects the page 'Jobs' to be displayed.
        await expect(page.url().includes('jobs')).toBeTruthy();
    });

    test('navigate to resources', async ({ page }) => {
        // Go to page 'Resources'.
        await page.getByRole('link', { name: 'Resources' }).click();
        await page.waitForURL('**/#/resources');

        // Expects the page 'Resources' to be displayed.
        await expect(page.url().includes('resources')).toBeTruthy();
    });
});
