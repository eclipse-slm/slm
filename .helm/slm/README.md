# Helm definitions for install on kubernetes

In this README we will give you a short introduction to install a k3s single cluster. Also, we want to give a short introduction 
how we can install [eclipse-slm/slm](https://github.com/eclipse-slm/slm) on our created k3s cluster. For this we use 
the [Helm Chars](https://helm.sh/) tool.  
For this we fist install k3s on our node. Then we install Helm Charts to deploy eclipse-slm easily.
After this we install eclipse-slm.

## Managing k3s

### Install
For a more detailed description how to install k3s, visit the official [Quick-Starter Guide](https://docs.k3s.io/quick-start)
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

#### From Apt (Debian/Ubuntu) 

```
curl https://baltocdn.com/helm/signing.asc | gpg --dearmor | sudo tee /usr/share/keyrings/helm.gpg > /dev/null
sudo apt-get install apt-transport-https --yes
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/helm.gpg] https://baltocdn.com/helm/stable/debian/ all main" | sudo tee /etc/apt/sources.list.d/helm-stable-debian.list
sudo apt-get update
sudo apt-get install helm
```

### Install SLM

Clone the repository from [eclipse-slm/slm](https://github.com/eclipse-slm/slm)
`git clone https://github.com/eclipse-slm/slm.git`

### Local installation from repository

`helm install slm slm/.helm/slm/ `

We can also install it in an extra namespace. For this we use the following command

`helm install slm slm/.helm/slm/ -n <your-namespace>  --create-namespace `

#### Upgrade  
If you did some changes you can upgrade the SLM with the command

`helm upgrade slm slm/.helm/slm/`

If you use a namespace, then you also need to add `-n <your-namespace>`

#### Uninstall   

If you want to remove the installation, then you can use the following command.  
`helm delete slm`  
If you use a namespace, then you also need to add `-n <your-namespace>`

To remove the created data by the system we delete the following folder with the 
command  
`sudo rm -Rf /data/slm`





### From Repository

TODO:  Create helm repo 

[//]: # (`helm repo add slm https://github.com/eclipse-slm/slm`  )

[//]: # (`helm repo update`  )

[//]: # (`helm search repo slm`  )

[//]: # (`helm install slm/slm slm/slm`  )
