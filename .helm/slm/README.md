# Helm definitions for install on kubernetes

This Chart installs the SLM resources 

## minikube

curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube

create folder under /data/minikube/ and copy the custom nginx.config to it for the awx-web container

minikube start --driver=docker --addons=ingress --cpus=6  --install-addons=true --kubernetes-version=stable --memory=8g --apiserver-ips=10.3.7.176
minikube start --driver=kvm2 --addons=ingress --cpus=6  --install-addons=true --kubernetes-version=stable --memory=8g
minikube start --driver=none --addons=ingress  --install-addons=true --kubernetes-version=stable 


### Dashboard
kubectl proxy --address 0.0.0.0 kubernetes-dashboard 8001:80 --namespace=kubernetes-dashboard --disable-filter=true
http://<ip-of-vm>:8001/api/v1/namespaces/kubernetes-dashboard/services/http:kubernetes-dashboard:/proxy/#/pod?namespace=default

## Install  
To install we can use one of the following commands. The last one will create also an extra 
namespace in kubernetes. 

## Slm via Helm

### Local installation from repository

`helm install slm ./.helm/slm/ `

#### Upgrade  
`helm upgrade slm ./.helm/slm/`

#### Delete  
`helm delete slm`



### From Repository
TODO:  Create helm repo for updates of the definitions
`helm repo add slm https://github.com/eclipse-slm/slm`  
`helm repo update`  
`helm search repo slm`  
`helm install slm/slm slm/slm`  
