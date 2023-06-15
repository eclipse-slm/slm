# Helm definitions for install on kubernetes

This Chart installs the SLM resources 

## Install  
To install we can use one of the following commands. The last one will create also an extra 
namespace in kubernetes. 


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
