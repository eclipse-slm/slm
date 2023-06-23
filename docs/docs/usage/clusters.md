---
permalink: /docs/usage/clusters
---

# Clusters
The Service Lifecycle Management enables the [installation of new clusters](#install-new-clusters) and the integration of [existing clusters (managed clusters)](#add-managed-clusters). To deploy a service offering into a cluster, the corresponding deployment capability must have the deployment type of the service offering defined in the property `supportedDeploymentTypes`(see section [Deployment Capabilites](/docs/usage/capabilities/#deployment-capabilities) for details).

## Install new clusters
A corresponding cluster deployment capability must be available to allow the installation of a new cluster. That deployment capability must have an `INSTALL` action and a definition for `clusterMemberTypes`.
<br><br>
Example from the [official K8S deployment capability](https://github.com/FabOS-AI/fabos-slm-dc-k8s):
```yaml
{
    ...
    "actions": {
        ...
        "INSTALL": {
        "capabilityActionClass": "AwxCapabilityAction",
        "capabilityActionType": "INSTALL",
        "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-k8s",
        "awxBranch": "main",
        "playbook": "install.yml",
        ...
    },
    ...
    "clusterMemberTypes": [
        {
            "name": "k8s_master",
            "prettyName": "Control-Plane Master",
            "minNumber": 3,
            "scalable": false
        },
        {
            "name": "k8s_node",
            "prettyName": "Worker",
            "minNumber": 1,
            "scalable": true
        },
        {
            "name": "haproxy",
            "prettyName": "External Proxy",
            "minNumber": 1,
            "scalable": false
        }
    ],
    ...
}
```

### Install new Kubernetes clusters
The Service Lifecycle Management provides an [official K8S deployment capability](https://github.com/FabOS-AI/fabos-slm-dc-k8s), which supports the installation of new clusters. At least 5 hosts are required to set up a new Kubernetes cluster.

Example:
* Go to the `Resource` tab and press the `+` button in the bottom right corner.
  <img :src="$withBase('/img/figures/use/cluster-create-new-1.png')">

* Select `ADD EXISTING`.
  <img :src="$withBase('/img/figures/use/cluster-create-new-2.png')">

* Select `Cluster`.
  <img :src="$withBase('/img/figures/use/cluster-create-new-3.png')">

* Configure cluster
  * Select `Create Methode` `Use existing resources`.
  * Select `Cluster Type` `Kubernetes`.
  *  Assign the hosts to the member types.
  * Press `CREATE`, and the automation to set up your Kubernetes cluster will be triggered. It will take a few minutes.

<img :src='/img/figures/use/cluster-create-new-4.png')">

* After the automation has finished, your new Kubernetes cluster will be shown in the `Resources` overview.
  <img :src="$withBase('/img/figures/use/cluster-create-new-5.png')">

## Add managed clusters
Already existing clusters be added as managed clusters. To allow the addition of a managed cluster, a corresponding cluster deployment capability must be available. That deployment capability must have an `INSTALL` action with the property `skipable: true`. Configuration parameters in the action, can be used to query information required for accessing the cluster.
<br><br>
Example from the [official K8S deployment capability](https://github.com/FabOS-AI/fabos-slm-dc-k8s):
```yaml:line-numbers
{
    ...
    "actions": {
        ...
        "INSTALL": {
            "capabilityActionClass": "AwxCapabilityAction",
            "capabilityActionType": "INSTALL",
            "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-k8s",
            "awxBranch": "main",
            "playbook": "install.yml",
            "skipable": true,
            "configParameters": [
                {
                "name": "kubeconfig",
                "prettyName": "Kube Config",
                "description": "...",
                "secret": true,
                "valueType": "FILE",
                "requiredType": "SKIP"
                },
                {
                "name": "namespace",
                "prettyName": "Namespace",
                "description": "...",
                "secret": true,
                "valueType": "STRING",
                "requiredType": "SKIP"
                }
            ]
        },
        ...
    },
    ...
}
```

### Kubernetes Clusters
The Service Lifecycle Management provides an [official K8S deployment capability](https://github.com/FabOS-AI/fabos-slm-dc-k8s), which supports managed clusters. It requires a [kubeconfig file](https://kubernetes.io/docs/concepts/configuration/organize-cluster-access-kubeconfig/) and a namespace in the cluster. The Service Lifecycle Management will deploy all services into this namespace.

Example:
* Go to the `Resource` tab and press the `+` button in the bottom right corner.
  <img :src="$withBase('/img/figures/use/cluster-add-existing-1.png')">

* Select `ADD EXISTING`.
  <img :src="$withBase('/img/figures/use/cluster-add-existing-2.png')">

* Select `Cluster`.
  <img :src="$withBase('/img/figures/use/cluster-add-existing-3.png')">

* Add details of cluster 
  * Select `Cluster Type` `Kubernetes`.
  * Upload or paste the kubeconfig file of the Kubernetes cluster you want to add.
  * Enter the name of the namespace where the Service Lifecycle Management should deploy your services.
  * Press `ADD`, and your Kubernetes cluster will be added.

  <img :src="$withBase('/img/figures/use/cluster-add-existing-4.png')">

* Your Kubernetes cluster will be shown in the `Resources` overview.
  <img :src="$withBase('/img/figures/use/cluster-add-existing-5.png')">
