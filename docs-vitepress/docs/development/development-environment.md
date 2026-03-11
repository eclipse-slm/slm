---
permalink: /docs/development/development-environment/
---

# Development Environment

## Local development

**1) Setup SLM instance**
To setup a SLM instance with the latest snapshot version follow the [installation instructions](/docs/getting-started/installation/) 
and use the latest snapshot version as tag or the SLM installer docker image (e.g. `ghcr.io/eclipse-slm/slm/installer:1.4.0-SNAPSHOT`).

**1) Export Configuration**

Edit the `stack/config-exporter/.env` file and set the following variable values according to your development setup:

|                      |                                                                                                                               |
|----------------------|-------------------------------------------------------------------------------------------------------------------------------|
| `LOCAL_PROJECT_PATH` | Path to the checked out [SLM git repository](https://github.com/eclipse-slm/slm). If you are using Docker Desktop for Windows add the above path in `Settings -> Resources -> FILE SHARING`.                                            |
| `SLM_HOSTNAME`       | Hostname of the host where your SLM instance is running.                                                                      |
| `LOCAL_HOSTNAME`     | Hostname of your local development machine.                                                                                   |
| `CONSUL_SCHEME`      | Scheme of the host where the Consul instance of the SLM is running.                                                           |
| `CONSUL_HOST`        | Hostname of the host where the Consul instance of the SLM is running.                                                         |
| `CONSUL_PORT`        | Port of the host where the Consul instance of the SLM is running.                                                             |
| `CONSUL_TOKEN`       | Master token of the Consul instance of your SLM instance. The Consul master token is printed at the end of the installer run. |

Run the config exporter sing the following command inside the directory `stack/config-exporter`:
```
docker-compose up
```

**3) IDE**

Stop the containers relevant for your development and start the components in your IDE. Through the Config Exporter, 
your local project has been configured to allow the components started via IDE to connect to the locally deployed SLM 
instance.
