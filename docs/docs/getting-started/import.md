---
permalink: /docs/getting-started/import/
---

# Import
After initial setup, publicly available content (e.g., service offerings) can be imported into your instance of the Service Lifecycle Management or
resources can be imported into the Resource Management.

## Service Management
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

## Resource Management
Resources and capabilities can be imported directly via the Resource Management importer API.

```text
https://<<your-slm-host>>/resource-management/importer
```

Use <a href="/files/importer/devices.xlsx" download="devices.xlsx">devices.xlsx</a> as example input file for the import. The importer reads the `Devices`
sheet and expects the following columns:

| **column** | **type** | **optional** | **description** |
|------------|----------|-------------------|-----------------|
| Name | string | no                | Device display name in the Excel file |
| Hostname | string | no                | Hostname of the resource. |
| IP Address | string | no                | IP address of the resource. |
| Is Resource | string | no                | Import flag. Use `yes` to import the row as resource. |
| Resource ID | UUID (string) | no                | Unique resource identifier. |
| Connection Type | string | yes               | Remote access connection type (e.g., `ssh`). |
| Connection Port | number | yes               | Remote access port (e.g., `22`). |
| Username | string | yes               | Remote access username. |
| Password | string | yes               | Remote access password. |
| Location ID | UUID (string) | yes               | ID of the location from the `Locations` sheet. |
| Asset ID | string | yes               | Asset identifier of the resource. |
| Firmware Version | string | yes               | Firmware version value for the resource. |
| Capabilities | JSON array of strings | yes               | Capability names from the `Capabilities` sheet, e.g. `["BaseConfig", "Docker:skip"]`. The suffix `:skip` marks a capability as linked but not installed. |

### Import resources from Excel Sheet

Endpoint:
```text
POST /importer/resources
```

Request parts:
- `file`: Multipart file (e.g., `devices.xlsx`)
- `fullPathOwnerGroupId`: Full path group ID of the owner of the imported resources (e.g., `/users/266a635c-cbeb-426d-b833-dfab1f8364ba`)

Example:
```sh
curl --request POST \
  --url https://<<your-slm-host>>/resource-management/importer/resources \
  --header 'Authorization: Bearer <<access_token>>' \
  --form 'file=@devices.xlsx' \
  --form 'fullPathOwnerGroupId=/users/266a635c-cbeb-426d-b833-dfab1f8364ba'
```

### Import capabilities from Excel Sheet

Endpoint:
```text
POST /importer/capabilities
```

Request parts:
- `file`: Multipart file (e.g., `devices.xlsx`)
- `fullPathOwnerGroupId`: Full path group ID of the owner of the imported resources (e.g., `/users/266a635c-cbeb-426d-b833-dfab1f8364ba`)
- `forceInstall`: If true the installation will be forced for all capabilities, even if they are already linked to the resource. This can be used to update 
   the capabilities of a resource. Default value is false.

Example:
```sh
curl --request POST \
  --url https://<<your-slm-host>>/resource-management/importer/capabilities \
  --header 'Authorization: Bearer <<access_token>>' \
  --form 'file=@devices.xlsx' \
  --form 'fullPathOwnerGroupId=/users/266a635c-cbeb-426d-b833-dfab1f8364ba' \
  --form 'forceInstall=true'
```
