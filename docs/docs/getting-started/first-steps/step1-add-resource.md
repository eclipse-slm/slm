---
permalink: /docs/getting-started/first-steps/step1/
prev: /docs/getting-started/first-steps/
---

# Step 1: Add a resource

Log in to the Service Lifecycle Management user interface running on port 8080 of your host (e.g. http://myhost.local:8080). If not changed, default user is `fabos` with password `password`.

First, you need to add a resource which will be managed by the Service Lifecycle Management. Go to the `Resources` section and click on the `+` button in the lower right corner:
<img :src="$withBase('/img/figures/first-steps/first-steps-step1-add-resource-button.png')">

Select `ADD EXISTING HOST`, enable `SSH access to resource` and enter the credentials of your device:
<img :src="$withBase('/img/figures/first-steps/first-steps-step1-add-resource-dialog.png')">
::: warning ATTENTION
User must be in group `sudo`
:::

As a result you should see your add device as resource in the overview:
<img :src="$withBase('/img/figures/first-steps/first-steps-step1-add-resource-result.png')">
