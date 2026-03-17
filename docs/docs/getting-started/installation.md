---
permalink: /docs/getting-started/installation/
next: /docs/getting-started/first-steps/
---

# Installation

## Prerequisites
* Docker

## Install

Set in your current shell the environment variables `SLM_HOSTNAME`, `SLM_IP` and `SLM_VERSION` for the host where the stack will be started. E.g.:
```sh
export SLM_HOSTNAME=myhost.local
export SLM_IP=172.17.0.1
export SLM_VERSION=1.4.0-SNAPSHOT
```
::: warning ATTENTION
**Use lowercase for the hostname to avoid case problems (e.g. with token authentication)**
:::

By default, the installer sets up the SLM on the host on which it is running. To install the SLM on a remote host, the
following environment variables must be set and added via the `--env` flag to the `docker run` command above:

| Environment Variable     | Description                       |
|--------------------------|-----------------------------------|
| DEPLOYMENT_HOST_HOSTNAME | Hostname or ip of the remote host |
| DEPLOYMENT_HOST_USER     | SSH username of remote host       |
| DEPLOYMENT_HOST_PASSWORD | SSH password of remote host       |

To start the installation either start the installer container via docker run or use the installer script (see the 
following sections for details). Both options will execute the same installation routine.

::::tabs
== docker run
::: warning For Ubuntu 24+ users
**Disable Apparmor for rsyslogd as follows:**
```sh
sudo ln -s /etc/apparmor.d/usr.sbin.rsyslogd /etc/apparmor.d/disable/
sudo apparmor_parser -R /etc/apparmor.d/usr.sbin.rsyslogd
```
:::

Run the following command to start the SLM installer:
```sh
docker run \
  --rm \
  --name eclipse-slm-installer \
  --pull=always \
  --env SLM_HOSTNAME=$SLM_HOSTNAME \
  --env SLM_IP=$SLM_IP \
  --volume /var/run/docker.sock:/var/run/docker.sock \
  --add-host $SLM_HOSTNAME:host-gateway \
  ghcr.io/eclipse-slm/slm/installer:$SLM_VERSION
```
==
== installer script
```shell
wget https://raw.githubusercontent.com/eclipse-slm/slm/main/stack/run-installer.sh
chmod +x run-installer.sh
./run-installer.sh
```
==
::::

SLM initializes its own PKI in Vault, including a dedicated Root CA, and issues certificates used by Traefik. Traefik acts as the HTTPS reverse proxy 
and terminates TLS for incoming traffic. If you want to use your own certificate chain, place an additional proxy in front of Traefik. For more details, 
see [Architecture - HTTPS](./architecture#https).

## Uninstall
Run the following command to start the SLM uninstaller:
```sh
docker run \
  --rm \
  --volume /var/run/docker.sock:/var/run/docker.sock \
  ghcr.io/eclipse-slm/slm/uninstaller:1.4.0-SNAPSHOT
```

## Components

### Ports
The different components of the stack can be reached under the following ports:
* AWX: [http://myhost.local:80]()
* Consul: [http://myhost.local:8500]()
* Keycloak: [http://myhost.local:7080]()
* Resource Registry: [http://myhost.local:9010]()
* Service Registry: [http://myhost.local:9020]()
* Vault: [http://myhost.local:8200]()
* UI: [http://myhost.local:8080]()

::: warning ATTENTION
**You need to replace `myhost.local` with the hostname of the host where you have installated the Service Lifecycle Management (see section [Start](#start)).**
:::

### Get configuration

Most of the settings and credentials are created automatically during first start of the stack. If you want to access the different components of the Service Lifecycle Management stack you need to export the configuration. Wait until the stack is fully started and all init containers have stopped. Then run the config exporter container:
```
docker-compose up --force-recreate config-exporter
```

It will generate by default a sub-directory `config/_conf_generated` relative to your docker-compose.yml file containing 
the configuration of the setup stack (`slm-config.yml`). If you want another target directory edit in file `config-exporter.yml` 
the host path of this volume:
```
- ./config:/project
```

## Known Issues

### Default Deployment Capabilities missing

By default the SLM setup routine adds deployment capabilities for

- docker
- docker-swarm
- k3s

which are added during the initial startup. In rare cases this adding process fails and consequently no deployment 
capabilities are available in the UI:

<figure>
    <img src='/img/figures/installation/known-issues-missing-dcs-dc-button-disabled.png' alt="known-issues-missing-dcs-dc-button-disabled">
    <figcaption>Deployment Capability Button disabled because of missing single host deployment capabilities</figcaption>
</figure>

<figure>
    <img src='/img/figures/installation/known-issues-missing-dcs-cluster-button-disabled.png'>
    <figcaption>Cluster Button disabled because of missing multi host deployment capabilities</figcaption>
</figure>

To fix this start the container `resource-registry-init` again by running:

```
docker-compose up -d resource-registry-init
```

After the container has started and has added the deployment capabilities and it will stop by itself. The capabilities (single 
host and cluster) should be available after reloading the ui:

<figure>
    <img src='/img/figures/installation/known-issues-missing-dcs-dc-button-enabled.png'>
    <figcaption>Deployment Capability Button enabled</figcaption>
</figure>

<figure>
    <img src='/img/figures/installation/known-issues-missing-dcs-cluster-button-enabled.png'>
    <figcaption>Cluster Create Button enabled</figcaption>
</figure>

<figure>
    <img src='/img/figures/installation/known-issues-missing-dcs-cluster-types-available.png'>
    <figcaption>Default Cluster Types available in selection form</figcaption>
</figure>
