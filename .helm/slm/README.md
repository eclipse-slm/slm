# Helm definitions for install on kubernetes

This Chart installs the SLM resources 

## k3s

### Install
For a more detailed description visit the official [Quick-Starter Guide](https://docs.k3s.io/quick-start)
Also have a look for the [Requirements](https://docs.k3s.io/installation/requirements) to configure your system

`curl -sfL https://get.k3s.io | sh -`

After running this installation:
- The K3s service will be configured to automatically restart after node reboots or if the process crashes or is killed
- Additional utilities will be installed, including kubectl, crictl, ctr, k3s-killall.sh, and k3s-uninstall.sh
- A kubeconfig file will be written to /etc/rancher/k3s/k3s.yaml and the kubectl installed by K3s will automatically use it

### Remove K3s from the System

K3s will create a remove script under the path /usr/local/bin/k3s-uninstall.sh     



## Install Helm and SLM  

### Install Helm
Follow the instructions on [Installing Helm](https://helm.sh/docs/intro/install/)

### Install SLM

Clone the repository from [eclipse-slm/slm](https://github.com/eclipse-slm/slm) to your home directory  
`git clone https://github.com/eclipse-slm/slm.git`

Create a new file with the name value.yaml  
Add the following content:  

```
slm:
    awx:
        nginx_path: PATH_TO_YOUR_HOME_DIC
```

Now we replace PATH_TO_YOUR_HOME_DIC with the location of the slm folder.

### Local installation from repository

`helm install slm slm/.helm/slm/ -f value.yaml `

#### Upgrade  
If you did some changes you can upgrade the SLM with the command

`helm upgrade slm slm/.helm/slm/`

#### Uninstall   
`helm delete slm`
To remove the folder under data slm
`sudo rm -Rf /data/slm`





[//]: # (### From Repository)

[//]: # (TODO:  Create helm repo for updates of the definitions)

[//]: # (`helm repo add slm https://github.com/eclipse-slm/slm`  )

[//]: # (`helm repo update`  )

[//]: # (`helm search repo slm`  )

[//]: # (`helm install slm/slm slm/slm`  )
