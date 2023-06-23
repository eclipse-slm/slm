---
permalink: /docs/getting-started/first-steps/step4/
---

# Step 4: Create a service offering

As a developer of a service vendor you should now see the section `Service Vendor`:
::: tip INFO
If `Service Vendor` section is not visible refresh the whole page or log out and in again.
:::

<img :src="$withBase('/img/figures/first-steps/first-steps-step4-select-service-vendor.png')">

Click on the `ADD SERVICE OFFERING` button:
<img :src="$withBase('/img/figures/first-steps/first-steps-step4-add-service-offering-button.png')">

A step-by-step wizard will open to support the service offering creation process. Use the following data for Step 1:
| Field | Value |
|-------|-------|
| Service Name                  | Node-RED |
| Service Category              | *Select your created service category (see [Step 3](/docs/getting-started/first-steps/step3)*) |
| Deployment Type               | Docker Compose |
| Version                       | 2.2.2 |
| Short Description             | Node-RED provides a browser-based flow editor that makes it easy to wire together flows using the wide range of nodes in the palette. |
| Description                   | Node-RED is a programming tool for wiring together hardware devices, APIs and online services in new and interesting ways.  It provides a browser-based editor that makes it easy to wire together flows using the wide range of nodes in the palette that can be deployed to its runtime in a single-click. |
| Click here to select image    | <a href="/img/figures/first-steps/cover-node-red.png" download><img :src="$withBase('/img/figures/first-steps/cover-node-red.png')" width="256px"></img></a><br>(Click on image to download) |

Your first step should look like this:
<img :src="$withBase('/img/figures/first-steps/first-steps-step4-wizard-step1-result.png')">

In the next steps you need to enter some Docker Compose related information. Download the three files <a href="/files/first-steps/docker-compose/docker-compose.yml" download>docker-compose.yml</a>, <a href="/files/first-steps/docker-compose/dot-env" download=".env">.env</a> and <a href="/files/first-steps/docker-compose/env.list" download>env.list</a> or create it with the following content:

<a href="/files/first-steps/docker-compose/docker-compose.yml" download>`docker-compose.yml`</a>

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

<a href="/files/first-steps/docker-compose/dot-env" download=".env">`.env`</a>

```
NODERED_PORT=1880
```

<a href="/files/first-steps/docker-compose/env.list" download>`env.list`</a>

```
TZ=Europe/Berlin
```

Upload the files in the corresponding fields
1) `docker-compose.yml` in `Compose File`
   <img :src="$withBase('/img/figures/first-steps/first-steps-step4-wizard-step2-docker-compose.png')">

2) `env` in `.env File (Optional)`. Enable Service Option for `NODERED_PORT`:
   <img :src="$withBase('/img/figures/first-steps/first-steps-step4-wizard-step2-dot-env.png')">

3) `env.list` in `Environment Variable Files -> env.list`. Enable Service Option for `TZ`:
   <img :src="$withBase('/img/figures/first-steps/first-steps-step4-wizard-step2-env-list.png')">

Click on the `NEXT` button. All environment variables with `Service Option` enabled will be converted to a service option of the service offering. Service options are values that can be edited by the users during the order process. For this example enter the following information for the two service options:
<img :src="$withBase('/img/figures/first-steps/first-steps-step4-wizard-step3.png')">

Click on the `FINISH` button to create your service offering.
