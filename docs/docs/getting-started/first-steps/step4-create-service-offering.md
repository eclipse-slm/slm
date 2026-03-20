---
permalink: /docs/getting-started/first-steps/step4/
---

# Step 4: Create a service offering

As a developer of a service vendor you should now see the section `Service Vendor`:
::: tip INFO
If `Service Vendor` section is not visible refresh the whole page or log out and in again.
:::

![first-steps-step4-select-service-vendor](/img/figures/first-steps/first-steps-step4-select-service-vendor.png)

Creating a new service offering is a two step process. First, you need to create a service offering and then you need to 
create a version of the service offering. A service offering can have multiple versions, but for this example we will 
create only one version.

## Create a service offering

Click on the `ADD SERVICE OFFERING` button:
![first-steps-step4-add-service-offering-button](/img/figures/first-steps/first-steps-step4-add-service-offering-button.png)

Click on `Service Offering Wizard` to start creating a service offering:

![first-steps-step4-select-service-offering-wizard](/img/figures/first-steps/first-steps-step4-select-service-offering-wizard.png)

Use the following data for your first offering and hit `Create:

| Field | Value |
|-------|-------|
| Service Name                  | Node-RED |
| Service Category              | *Select your created service category (see [Step 3](/docs/getting-started/first-steps/step3-create-service-vendor.md)*) |
| Short Description             | Node-RED provides a browser-based flow editor that makes it easy to wire together flows using the wide range of nodes in the palette. |
| Description                   | Node-RED is a programming tool for wiring together hardware devices, APIs and online services in new and interesting ways.  It provides a browser-based editor that makes it easy to wire together flows using the wide range of nodes in the palette that can be deployed to its runtime in a single-click. |
| Click here to select image    | <img src="/img/figures/first-steps/cover-node-red.png" alt="Node-RED Cover Image" width="200" /> |

Your first offering should now be created and you should see the following screen:
![first-steps-step4-overview-with-new-offering](/img/figures/first-steps/first-steps-step4-overview-with-new-offering.png)

## Create a version of a service offering

By clicking the `+` button in the actions column of your new offering the wizard for creating a new version of your 
offering will appear. For your first version keep the default version number `1.0.0.` and select `Docker Compose` as 
deployment type. Then confirm by clicking the `NEXT` button:

![](/img/figures/first-steps/first-steps-step4-wizard-step1-result.png)

In the next steps you need to enter some Docker Compose related information. Download the three files 
<a href="/slm/public/files/first-steps/docker-compose/docker-compose.yml" download>docker-compose.yml</a>, 
<a href="/slm/public/files/first-steps/docker-compose/dot-env.txt" download>.env</a> and 
<a href="/slm/public/files/first-steps/docker-compose/env.list" download>env.list</a> or create it with the following content:

<a href="/slm/public/files/first-steps/docker-compose/docker-compose.yml" download>`docker-compose.yml`</a>

```
version: "3"

services:
  node-red:
    image: nodered/node-red:2.2.2
    env_file:
      - env.list
    ports:
      - "${NODERED_PORT}:1880"
```

<a href="/slm/public/files/first-steps/docker-compose/.env" download=".env">`.env`</a>

```
NODERED_PORT=1880
```

<a href="/slm/public/files/first-steps/docker-compose/env.list" download>`env.list`</a>

```
TZ=Europe/Berlin
```

Upload the files in the corresponding fields (field for `env` and `env.list` will appear afer uploading the 
`docker-compose.yml` file):
1) `docker-compose.yml` in `Compose File`
   ![](/img/figures/first-steps/first-steps-step4-wizard-step2-docker-compose.png)

2) `env` in `.env File (Optional)`. Enable Service Option for `NODERED_PORT`:
   ![](/img/figures/first-steps/first-steps-step4-wizard-step2-dot-env.png)

3) `env.list` in `Environment Variable Files -> env.list`. Enable Service Option for `TZ`:
   ![](/img/figures/first-steps/first-steps-step4-wizard-step2-env-list.png)

In case your docker compose workload requires credentials for pulling the images you can select those credentials
via the `Docker Registries Credentials` field. For this example we will not use any credentials, so just leave the field 
empty.

Click on the `NEXT` button. All environment variables with `Service Option` enabled will be converted to a service 
option of the service offering. Service options are values that can be edited by the users during the order process. 
For this example enter the following information for the two service options:
![](/img/figures/first-steps/first-steps-step4-wizard-step3.png)

The Service Options are grouped by `Service Option Categories`. A default category `Common` is already created for you
and `.env` and `env.list` variables do also a separate category. You can also create your own categories and arrange
the options as you wish. For this example we will use the default categories and delete `Common` as it is empty.

Click on the `NEXT` button to go to the requirements step. Here you can define requirements for your service offering. 
Requirements are used to find the right host for your service offering in case it requires specific hardware or software 
capabilities. For this example we will not define any requirements, so just click  `CREATE` to create your service offering.

![](/img/figures/first-steps/first-steps-step4-final-result.png)
