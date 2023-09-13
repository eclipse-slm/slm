import { test, expect } from '@playwright/test';

test.describe('login tests', () => {
    test.beforeEach(async ({ page }) => {
        // Go to FabOS login page.
        await page.goto('http://10.3.7.173:8080');
    });

    test('page has correct title', async ({ page }) => {
        // Expects the page to have 'Sign in to fabos' as title.
        await expect(page).toHaveTitle(/Sign in to fabos/);
    });

    test('sign in with valid data', async ({ page }) => {
        // Click in input fields and enter valid credentials.
        await page.getByLabel('Username or email').click();
        await page.getByLabel('Username or email').fill('fabos');
        await page.getByLabel('Password').click();
        await page.getByLabel('Password').fill('pasord');
        await page.getByRole('button', { name: 'Sign In' }).click();

        // Expects the user to be logged in.
        await expect(page.url().includes('auth')).toBeFalsy();
    });

    test('sign in with invalid data', async ({ page }) => {
        // Click in input fields and enter invalid credentials.
        await page.getByLabel('Username or email').click();
        await page.getByLabel('Username or email').fill('fabos');
        await page.getByLabel('Password').click();
        await page.getByLabel('Password').fill('invalidpassword');
        await page.getByRole('button', { name: 'Sign In' }).click();

        // Expects the user to be not logged in.
        await expect(page.url().includes('auth')).toBeTruthy();
    });
});
