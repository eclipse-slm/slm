---
permalink: /docs/getting-started/first-steps/step3/
---

# Step 3: Create service category and service vendor

Since your device is now ready for service deployment, we need to create a service offering that can be deployed on your 
device. Therefore, we need to create a service category and a service vendor in the `Admin` section. In case you do not 
see the `Admin` section at the bottom of the left menu:

![first-steps-step3-activated-admin-section](/img/figures/first-steps/first-steps-step3-activated-admin-section.png)

...please follow the instructions in the [next section](#make-default-user-a-admin) to make the default user an admin 
first. Otherwise, continue with the instructions in this [section](#create-service-vendor).

## Make default user a admin

In case you do not see the `Admin` section at the bottom of the left menu, you need to make the default user `fabos` an 
admin first. To do so, you have to log into SLM's keycloak instance (e.g. https://myhost.local/auth) with the default 
credentials (`admin` / `password`):

![first-steps-step3-login-keycloak](/img/figures/first-steps/first-steps-step3-login-keycloak.png)

:::info GO TO
Manage realms
:::

...and select the `fabos` realm:

![first-steps-step3-select-keycloak-realm](/img/figures/first-steps/first-steps-step3-select-keycloak-realm.png)

:::info GO TO
Users
:::

... and select the user `fabos`:

![first-steps-step3-select-fabos-user](/img/figures/first-steps/first-steps-step3-select-fabos-user.png)

::: info GO TO
Assign role > realm roles
:::

... select `slm-admin` and confirm with `Assign`:

![first-steps-step3-assign-admin-role](/img/figures/first-steps/first-steps-step3-assign-admin-role.png)

Logout from keyloak:

![first-steps-step3-logout-keycloak](/img/figures/first-steps/first-steps-step3-logout-keycloak.png)

## Create Service Vendor

:::info GO TO
Admin > Service Vendors
:::

... and click on `Add Service Vendor` button in the lower left corner:

![first-steps-step3-service-vendor-overview](/img/figures/first-steps/first-steps-step3-service-vendor-overview.png)

Enter a name, description and a logo (optional) for your service vendor and click `Create`:

![first-steps-step3-new-service-vendor-form](/img/figures/first-steps/first-steps-step3-new-service-vendor-form.png)

Make sure to add the user `fabos` as developer to your service vendor to be able to create and manage service offerings 
for this service vendor. To do so, select your created service vendor:

![first-steps-step3-select-new-service-vendor](/img/figures/first-steps/first-steps-step3-select-new-service-vendor.png)

click on `Add developer` field, select the user `fabos` from the dropdown 

![first-steps-step3-select-fabos-user-as-developer.png](/img/figures/first-steps/first-steps-step3-select-fabos-user-as-developer.png)

...and click `SAVE`.

## Create Service Offering Category

:::info GO TO
Admin > Service Categories
:::

... and add a new service category by clicking on the `Add Service Category` button in the lower left corner in case you 
do not have any service category yet or want to add another one:

![first-steps-step3-service-category-overview](/img/figures/first-steps/first-steps-step3-service-category-overview.png)

Enter a name for your service category and click `Create`:

![first-steps-step3-service-category-form](/img/figures/first-steps/first-steps-step3-service-category-form.png)
