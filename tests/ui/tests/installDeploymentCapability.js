import { test, expect } from '@playwright/test';
import { loginHelper, CURRENT_DIR } from '../helperFunctions/helperFunctions';

test('install a docker deployment capability', async ({ page }) => {
    // Go to FabOS website and log in with valid credentials.
    await loginHelper(page);
    // Expects the user to be logged in.
    await expect(page.url().includes('#')).toBeTruthy();

    await page.getByRole('link', { name: 'Resources' }).click();
    await page
        .getByRole('row', {
            name: 'Docker dia-linux-7b130ee6.dia.svc.fortknox.local 10.3.7.170 ssh 22 1881e5bf-e8d4-4e2f-a0ea-fc42d629cf7d false'
        })
        .getByRole('button')
        .first()
        .click();
    await page.getByRole('button', { name: 'Docker' }).click();
    await page.getByRole('link', { name: 'Jobs' }).click();
    await page.getByRole('link', { name: 'Resources' }).click();
    await page
        .getByRole('cell', { name: 'Docker' })
        .locator('span')
        .first()
        .click();
    await page.getByRole('button', { name: 'Close' }).click();
});
