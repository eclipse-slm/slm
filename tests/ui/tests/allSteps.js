import { test, expect } from '@playwright/test';
import { loginHelper, delay, CURRENT_DIR } from '../helperFunctions/helperFunctions';

test('all steps', async ({ page }) => {
    // Go to FabOS website and log in with valid credentials.
    await loginHelper(page);
    // Expects the user to be logged in.
    await expect(page.url().includes('#')).toBeTruthy();

    // Switch to page 'Resource' and take a screenshot.
    await page.getByRole('link', { name: 'Resources' }).click();

    // :TODO: page zoom
    //await page.evaluate(() => {
    //    document.body.style.zoom = 0.8;
    //});

    await page.waitForURL('**/#/resources', { waitUntil: 'domcontentloaded' });
    await page.screenshot({
        path: './screenshots/step_1_resource_page.png',
        fullPage: true
    });
    // Expects the page 'Resource' to be displayed.
    await expect(page.url().includes('resources')).toBeTruthy();

    // Click on button to add a new resource.
    await page.locator('[id="resources-button-add-resource"]').click();
    // Expects dialog to be open.
    // FIXME: Find useful identifier
    //await expect(
    //    page.locator('class="v-dialog v-dialog--active"')
    //).toBeVisible();

    // Click on 'add existing'.
    await page.getByRole('button', { name: 'Add existing' }).click();
    // Expect next dialog to be shown.
    await expect(page.getByText('Add existing resource')).toBeVisible();

    // Click on 'Host'.
    await page.getByRole('button', { name: 'Host' }).click();
    // Expect next dialog to be shown.
    await expect(page.getByText('Add existing host resource')).toBeVisible();

    // Activate remote access to resource and take screenshot.
    await page.locator('.v-input--selection-controls__ripple').click();
    await page.screenshot({
        path: './screenshots/step_1_enable_remote_access.png',
        fullPage: true
    });

    // Enter host name.
    await page.getByLabel('Hostname', { exact: true }).click();
    await page.getByLabel('Hostname', { exact: true }).fill('dia-linux-7b130ee6.dia.svc.fortknox.local');

    // Click in ip field and enter ip address.
    await page.locator('[id="resource-create-text-field-ip"]').click();
    await page.locator('[id="resource-create-text-field-ip"]').fill('10.3.7.170');

    // Select connection type 'ssh'.
    await page.getByRole('button', { name: 'Connection Type' });
    await page.getByRole('button', { name: 'Connection Type' }).click();
    await page.getByRole('option', { name: 'ssh', exact: true }).getByText('ssh').click();

    // Enter valid log in credentials of resource.
    await page.getByLabel('Username').click();
    await page.getByLabel('Username').fill('vfk');
    await page.getByLabel('Password').click();
    await page.getByLabel('Password').fill('bc24655a');

    // Click on button to add the resource.
    await page.getByRole('button', { name: 'Add' }).click();
    await page.getByRole('button', { name: 'Add' }).click();

    await page.screenshot({
        path: './screenshots/step_1_add_resource_success.png',
        fullPage: true
    });

    // :TODO: Assert that the resource was added successfully with the counter in the dashboard page?

    // -------------------------------------------------- INSTALL DOCKER DEPLOYMENT CAPABILITY --------------------------------------------------

    // Click on button to add a new deployment capability.
    await page.locator('[id="mushroom-button"]').click();
    // Click on Docker.
    await page.locator('[id="install-button"]').click();

    // Expect installation process to be running. TODO: Find a better way to check for this because it will timeout.
    //await expect(page.getByText("turned into state 'RUNNING'")).toBeVisible();

    // Switch to Jobs page and back to Resources page.
    await page.getByRole('link', { name: 'Jobs' }).click();
    await page.waitForURL('**/#/jobs');
    await expect(page.url().includes('jobs')).toBeTruthy();
    await page.screenshot({
        path: './screenshots/step_2_deploy_docker_job.png',
        fullPage: true
    });
    await page.getByRole('link', { name: 'Resources' }).click();
    await page.waitForURL('**/#/resources');
    await expect(page.url().includes('resource')).toBeTruthy();
    await page.screenshot({
        path: './screenshots/step_2_resource_with_docker_deployed.png',
        fullPage: true
    });

    // Expect docker deployment capability to be installed.
    // :TODO: Need to wait for the installation to finish and then check first column for 'Docker'.
    //await page
    //    .getByRole('cell', { name: 'Docker' })
    //    .locator('span')
    //    .first()
    //    .click();
    //await page.getByRole('button', { name: 'Close' }).click();

    // -------------------------------------------------- CREATE SERVICE CATEGORY --------------------------------------------------
    // Switch to page 'Admin'.
    await page.getByRole('link', { name: 'Admin' }).click();
    await page.waitForURL('**/#/admin');
    await expect(page.url().includes('admin')).toBeTruthy();
    await page.screenshot({
        path: './screenshots/step_3_admin_view.png',
        fullPage: false
    });
    // Click on button to add a new service category.
    await page.getByRole('button', { name: 'Add Service Category' }).click();
    await page.getByLabel('Name', { exact: true }).click();
    await page.screenshot({
        path: './screenshots/step_3_add_service_category.png',
        fullPage: false
    });
    await page.getByLabel('Name', { exact: true }).fill('PlaywrightTestServiceCategory');
    await page.getByRole('button', { name: 'Create' }).click();

    // -------------------------------------------------- CREATE SERVICE VENDOR --------------------------------------------------
    // Click on button to add a new service vendor.
    await page.getByRole('button', { name: 'Add Service Vendor' }).click();
    await page.getByLabel('Name', { exact: true }).click();
    await page.screenshot({
        path: './screenshots/step_3_add_service_vendor.png',
        fullPage: false
    });
    await page.getByLabel('Name', { exact: true }).press('Control+Shift+ArrowLeft');
    await page.getByLabel('Name', { exact: true }).press('Control+Shift+ArrowLeft');
    await page.getByLabel('Name', { exact: true }).press('Control+Shift+ArrowLeft');
    await page.getByLabel('Name', { exact: true }).fill('Playwright UI Testing Service Vendor');

    await page.getByRole('textbox', { name: 'Description' }).click();
    await page.getByRole('textbox', { name: 'Description' }).fill('Playwright');
    await page
        .locator('div')
        .filter({ hasText: /^Logo$/ })
        .locator('div')
        .click();
    await page.getByLabel('Logo').setInputFiles(CURRENT_DIR + '/testResources/Playwright.png');
    await page.getByRole('button', { name: 'Create' }).click();

    await page
        .getByRole('cell', {
            name: 'Playwright UI Testing Service Vendor',
            exact: true
        })
        .click();

    await page.getByRole('combobox').locator('div').nth(1).click();
    await page.getByText('fabos', { exact: true }).click();
    await page.getByRole('combobox').locator('i').click();
    await page.getByRole('button', { name: 'Save' }).click();

    await page.screenshot({
        path: './screenshots/step_3_add_fabos_as_developer.png',
        fullPage: false
    });

    // -------------------------------------------------- CREATE SERVICE OFFERING --------------------------------------------------

    await page.getByRole('link', { name: 'Service Vendor' }).click();
    await page.getByRole('button', { name: 'Select Service Vendor' }).click();
    await page.getByRole('option', { name: 'Playwright UI Testing Service Vendor' }).click();
    await page.getByRole('button', { name: 'Add Service Offering' }).click();
    await page.getByRole('button', { name: 'Service Offering Wizard' }).click();
    await page.url().includes('creationType=manual');

    //-------------------------------------------------------------------------------------------------------------------------------
    //await page.getByLabel('Name').click();
    await page.locator('[id="serviceNameInput"]').click();
    await page.getByLabel('Name').fill('Node-RED');

    await page.getByRole('button', { name: 'Service Category' }).click();
    await page.getByText('PlaywrightTestServiceCategory').click();

    // TODO: Short Description and normal Description will be skipped for now.
    /** 
    await page.getByRole('input', { id: 'shortDescriptionInput2' }).click();
    await page.locator('[id="shortDescriptionInput2"]').click();

    await page.getByLabel('Short Description').click();
    await page.getByLabel('Short Description').fill('Node-RED provides a browser-based flow editor that makes it easy to wire together flows using the wide range of nodes in the palette.');
    await page.getByLabel('Description').click();
    await page
        .getByLabel('Description')
        .fill(
            'Node-RED is a programming tool for wiring together hardware devices, APIs and online services in new and interesting ways. It provides a browser-based editor that makes it easy to wire together flows using the wide range of nodes in the palette that can be deployed to its runtime in a single-click.'
        );
    */
    await page
        .locator('div')
        .filter({ hasText: /^Click here to select image$/ })
        .locator('div')
        .click();
    await page.getByLabel('Click here to select image').setInputFiles(CURRENT_DIR + '/testResources/cover-node-red.png');
    await page.getByRole('button', { name: 'Create' }).click();

    await page.getByRole('button', { name: '󰅀' }).click();
    await page.getByRole('button', { name: 'Create first version' }).click();

    // add version 2.2.2
    await page.getByLabel('Version').click();
    await page.getByLabel('Version').fill('2.2.2');
    // add deployment type
    await page.getByRole('button', { name: 'Deployment Type' }).click();
    await page.getByText('Docker Compose').click();

    await page.getByRole('button', { name: 'Next' }).click();
    await page
        .locator('div')
        .filter({ hasText: /^Click here to select Docker Compose file$/ })
        .locator('div')
        .click();
    await page.getByLabel('Click here to select Docker Compose file').setInputFiles(CURRENT_DIR + '/testResources/docker-compose.yml');

    // content of env file was moved to docker compose file - therefore this is not needed anymore
    /** 
    await page.getByRole('button', { name: '.env File (Optional)' }).click();
    await page
        .locator('div')
        .filter({ hasText: /^Click here to select \.env file$/ })
        .locator('div')
        .click();
    await page.getByLabel('Click here to select .env file').setInputFiles(CURRENT_DIR + '/testResources/env'); // after this, a second checkbox appears
    */

    // check checbkox 'Service Option'
    await page.locator('.v-input--selection-controls__ripple').click();
    await page
        .locator('div')
        .filter({ hasText: /^Click here to select env\.list$/ })
        .locator('div')
        .click();
    await page.getByLabel('Click here to select env.list').setInputFiles(CURRENT_DIR + '/testResources/env.list');
    // check checbkox 'Service Option'
    await page
        .locator(
            '.v-list-group__items > div > div:nth-child(3) > .v-data-table > .v-data-table__wrapper > table > tbody > tr > td:nth-child(3) > .v-input > .v-input__control > .v-input__slot > .v-input--selection-controls__input > .v-input--selection-controls__ripple'
        )
        .click();

    await page.getByRole('button', { name: 'Next' }).click();

    // description nodered
    await page.locator('[id="NODERED_PORT_Desc"]').click();
    await page.locator('[id="NODERED_PORT_Desc"]').fill('Port of Node-RED');
    // dropdown to select type PORT
    await page.getByRole('row', { name: 'NODERED_PORT' }).getByRole('button').click();
    await page.getByText('PORT', { exact: true }).click();
    // Value input
    await page.locator('[id="NODERED_PORT_Value"]').click();
    await page.locator('[id="NODERED_PORT_Value"]').fill('1880');
    // check editable checkbox
    await page.locator('#NODERED_PORT_EditableCheckbox i').click();

    // description timezone
    await page.locator('[id="TZ_Desc"]').click();
    await page.locator('[id="TZ_Desc"]').fill('Timezone');
    // dropdown to select type STRING
    await page.locator('[id="TZ_ValueType"]').click();
    await page.getByRole('option', { name: 'STRING' }).getByText('STRING').click();
    // Value input
    await page.locator('[id="TZ_Value"]').click();
    await page.locator('[id="TZ_Value"]').fill('Europe/Berlin');
    // check editable checkbox
    await page.locator('#TZ_EditableCheckbox i').click();

    await page.getByRole('button', { name: 'Next' }).click();
    await page.getByRole('button', { name: 'Create' }).click();
    // Step 4 done

    //-------------------------------------------------------------------------------------------------------------------------------

    /**
    // Step 5: Deploy service instance.
    await page.getByRole('link', { name: 'Service Offerings' }).click();
    await page.getByText('Node-RED').nth(2).click();
    await page.getByRole('button', { name: 'Order' }).click();

    // Select deployment resource
    await page.getByRole('button', { name: 'Deployment Resource' }).click();
    await page.getByText('dia-linux-7b130ee6.dia.svc.fortknox.local').click();
    await page.getByRole('button', { name: 'Checkout' }).click();

    // Switch to page 'Services'
    await page.getByRole('link', { name: 'Services' }).click();
    */
});
