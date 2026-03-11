---
permalink: /docs/getting-started/import/
---

# Import
After initial setup, publicly available content (e.g., service offerings) can be imported into your instance of the Service Lifecycle Management.

## Service Registry
To import content into the Service Registry of the Service Lifecycle Management local directories or git repositories can be used. See the two sections below for more information how to configure import from local directories or git repositories. The import can be started using this command:
``` sh
docker-compose up --force-recreate service-registry-init
```

### Local Directories
Update in the `.env` file in the `compose` directory the variable `SERVICE_REGISTRY_INITIALIZATION_LOCAL_DIRECTORIES` with your directories. Multiple directories must be seperated by comma. The defined directories must be added as volumes to the `service-registry-init` service in the `docker-compose.yml`.

Example of `.env`:
``` sh
SERVICE_REGISTRY_INITIALIZATION_LOCAL_DIRECTORIES=/my/local/dir1,/my/local/dir2
``` 

Example of `docker-compose.yml`:
``` yaml
...
  service-registry-init:
    ...
    volumes:
    ...
    - "/my/local/dir1:/my/local/dir1"
    - "/my/local/dir2:/my/local/dir2"
...
```

### Git Repositories
Update in the `.env` file in the `compose` directory the variable `SERVICE_REGISTRY_INITIALIZATION_GIT_REPOS` with your git repositories. Default value is the public [service registry content repository](https://github.com/FabOS-AI/fabos-slm-service-registry-content). Multiple git repositories must be seperated by comma. Authentication for a git repository can be defined in the form `{Username}:{PasswordOrAccessToken}@{RepoUrl}`.

Example:
``` sh
SERVICE_REGISTRY_INITIALIZATION_GIT_REPOS=https://github.com/FabOS-AI/fabos-slm-service-registry-content.git,myGitUser:myGitUserPassword@https://my-private-git-repo.git
```

## Resource Registry

To populate the Resource Registry of the Service Lifecycle Management with an already available list of known, a simple utility tool can be used to speed up the process. The tool can be found on [GitHub](https://github.com/FabOS-AI/fabos-slm-resource-registry-init). In the current version, the tool parses a given **Excel file** and populates the Resource Registry with the contained resources.

::: warning ATTENTION
The tool was initially only intended for a quick repopulation of the Resource Registry in demo setups. Consequently, the tool is rudimentary in design and will be changed in the future in order to be more aligned with the SLM base setup and components.
:::

Since the tool interacts with the REST-API of the ``Resource Registry``, to use the tool just clone the repo into any directory:
``` sh
git clone https://github.com/FabOS-AI/fabos-slm-resource-registry-init && cd fabos-slm-resource-registry-init
``` 

Now the Excel file `example.xlsx` needs to be adapted to the list of resources to be added to the Resource Registry. There are two example devices listed to familiarize with the pattern. The following fields are definitely required to be filled (per device):
| **variable** | **type** | **description**                                                                                          |
|--------------|----------|----------------------------------------------------------------------------------------------------------|
| Device       | string   | An arbitrary device name, for easier reference                                                           |
| user         | string   | The user name of the resource, in order to be accessed                                                   |
| password     | string   | The password of the corresponding user                                                                   |
| hostname     | string   | The hostname of the resource                                                                             |
| eth0 IP      | string   | The IP of the resource. Additionally, eth1 IP can be filled as well, but eth0 will be prefered           |
| is_resource  | string   | A flag that determines if device is added to the resource registry. Set it to "yes" in order to be added |
| UUID         | string   | The UUID of the resource, can be generated externally                                                    |

Additionally, following flags can be used to trigger the install of an capability for the resource (optional):
| **variable** | **type** | **description**                                              |
|--------------|----------|--------------------------------------------------------------|
| DC_Docker    | string   | If set to "yes", Docker capability will be added to resource |
| DC_Swarm     | string   | If set to "yes", Swarm capability will be added to resource  |
| DC_K3s       | string   | If set to "yes", K3s capability will be added to resource    |


In a second step, the settings for the tool need to be adapted and verified. Therefore open the `docker-compose.yaml` in a text editor and change the variables in the `environment`-section accoding to your local environment. When the Service Lifecycle Management was setup through the inital setup, all variables should already be matching the local environment. See the following table for details on the settings:

| **variable**    | **type** <div style="width:110px"/>              | **default**                   | **description** <div style="width:500px"/>                                                                                                        |
|-----------------|--------------------------------------------------|-------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| SLM_HOST        | string                                           | "http://172.17.0.1"           | The full qualified domain name of the SLM host or IP address, including the http prefix                                                           |
| SLM_USER        | string                                           | "fabos"                       | The user to be used to add the resources to the SLM                                                                                               |
| SLM_PASSWORD    | string                                           | "password"                    | The corresponding password for the SLM user                                                                                                       |
| XLSX_FILE       | string                                           | "/files/example.xlsx"         | The Excel file inside the container. On default all file of the cloned directory are mounted under "/files"                                       |
| SHEET_NAME      | string                                           | "DEVICES"                     | The sheet name inside the Excel file which contains the list of resources                                                                         |
| FORCE_OVERWRITE | bool (as string)                                 | "False"                       | Determines if resources and their capabilites should be overwritten if they already exist                                                         |
| FORCE_DELETE    | bool (as string)                                 | "False"                       | Determines if a resources listed in the EXCEL sheet should be deleted in the first step                                                           |
| DELETE_ALL      | bool (as string)                                 | "False"                       | Determines if all resources (not only listed resources in the EXCEL) should be deleted in the first step, to start with a clean resource registry |
| PING_CHECK      | bool (as string)                                 | "False"                       | Determines if resources should be pinged before added to the resource registry                                                                    |

::: warning ATTENTION
Especially in a setup with docker be aware of the correct host used in the ``SLM_HOST`` variable. In Docker **localhost** and **127.0.0.1** will not work, instead try the magic internal Docker host **host.docker.internal**, **172.17.0.1** or in best case the regular IP of the host.
:::

Finally, the tool can be run through Docker. Therefore use Docker-Compose to build and run the tool:
``` sh
docker-compose up --build
```

A successful run of the tool will print a summary at the end of the run. For the given resource in the `example.xlsx` file, the summary looks like this:

```
resource-registry-init  | SUMMARY -------------------------------------------------------------------------------------------------------------------
resource-registry-init  | Resources deleted (via REST): []
resource-registry-init  | Resources accessible (via ping): []
resource-registry-init  | Resources added to registry (via REST): [
resource-registry-init  |   "0226a355-3156-43e8-b51e-3b670c6ba7c3, SOCP2001-20011111, 192.168.153.48",
resource-registry-init  |   "7fd248f5-e698-46f1-b30e-1ac568d66c15, RevPi30562, 192.168.153.51"
resource-registry-init  | ]
resource-registry-init  | Capabilities added to resources (via REST): [
resource-registry-init  |   "7fd248f5-e698-46f1-b30e-1ac568d66c15, RevPi30562, ['DOCKER']"
resource-registry-init  | ]
resource-registry-init  | Resource Registry Setup Done!
resource-registry-init  | Took: 24.11s
```
This indicates that the two listed resources (``SOCP2001-20011111``, ``RevPi30562``) were successfully added to the Resource Registry and that the `Docker` capability was also added for the `RevPi` device.