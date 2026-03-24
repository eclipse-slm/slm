---
permalink: /docs/getting-started/first-steps/step1/
prev: /docs/getting-started/first-steps/
---

# Step 1: Add a resource

Log in to the Service Lifecycle Management (SLM) user interface of your host (e.g. https://myhost.local). As the SLM
comes with a self-signed https certificate you will get a warning in your browser when trying to access the UI for the first time.
You can ignore this warning and proceed to the UI:

![ssl-warning](/img/figures/first-steps/first-steps-step0-ignore-self-signed-cert.png)

If not changed, default user is `fabos` and password `password`.

First, you need to add a resource which will be managed by the Service Lifecycle Management. 

::: info GO TO
Devices > Instances
:::

...and click on the `+` button in the lower right corner:

![first-steps-step1-add-resource-button](/img/figures/first-steps/first-steps-step1-add-resource-button.png)

Enter hostname (if not available choose as you like), a valid IP address and click on `Create Device`:

![first-steps-step1-add-device-dialog](/img/figures/first-steps/first-steps-step1-add-device-dialog.png)

If you do have remote access to the device select `Connection Type`, `Connection Port` and `Credential Type` in the next 
step. Otherwise click `Skip` to add the device without connection information. You can add this information later on if needed.

::: warning ATTENTION
User must be in group `sudo` in case you have selected `ssh` as `Connection Type`.
:::

Depending on which `Credential Type` you have selected, you will be asked to enter the corresponding credentials.
For example, if you have selected `Username / Password` you need to enter a valid username and password for the device
and a credential name:

::::tabs
== Username / Password
![first-steps-step1-add-resource-credentials-dialog-username-password](/img/figures/first-steps/first-steps-step1-set-cred-username-password.png)
==

== SSH Key
![first-steps-step1-add-resource-credentials-dialog-ssh-key](/img/figures/first-steps/first-steps-step1-set-cred-ssh-key.png)
==
::::

Finally, finish adding the device by click `Add Remote Access`.


As a result you should see your add device as resource in the overview:
![first-steps-step1-add-resource-result](/img/figures/first-steps/first-steps-step1-add-resource-result.png)
