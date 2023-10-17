(window.webpackJsonp=window.webpackJsonp||[]).push([[39],{353:function(t,a,e){"use strict";e.r(a);var s=e(15),n=Object(s.a)({},(function(){var t=this,a=t._self._c;return a("ContentSlotsDistributor",{attrs:{"slot-key":t.$parent.slotKey}},[a("h1",{attrs:{id:"capabilities"}},[a("a",{staticClass:"header-anchor",attrs:{href:"#capabilities"}},[t._v("#")]),t._v(" Capabilities")]),t._v(" "),a("h2",{attrs:{id:"deployment-capabilities"}},[a("a",{staticClass:"header-anchor",attrs:{href:"#deployment-capabilities"}},[t._v("#")]),t._v(" Deployment Capabilities")]),t._v(" "),a("p",[t._v("Deployment Capabilities are used to deploy a service on a resource. They can be added dynamically via the REST API of the "),a("RouterLink",{attrs:{to:"/docs/usage/api/#resource-registry"}},[t._v("Resource Registry")]),t._v(". To simplify the use of the API the requests related to capabilites are available in the "),a("a",{attrs:{href:"https://www.postman.com/fabos-ai/workspace/service-lifecycle-management/folder/22732344-bc9fe87a-30cc-41aa-a3d9-4d1d39a1f4e4",target:"_blank",rel:"noopener noreferrer"}},[t._v("public Postman workspace"),a("OutboundLink")],1),t._v(". The initial setup of Postman is described "),a("RouterLink",{attrs:{to:"/docs/usage/api/#postman"}},[t._v("here")]),t._v(".")],1),t._v(" "),a("h3",{attrs:{id:"import-officially-supported-deployment-capabilities"}},[a("a",{staticClass:"header-anchor",attrs:{href:"#import-officially-supported-deployment-capabilities"}},[t._v("#")]),t._v(" Import officially supported Deployment Capabilities")]),t._v(" "),a("p",[t._v("After installation, a new Service Lifecycle Management instance has no deployment capabilities. By default, the compose stack of the Service Lifecycle Management includes a service "),a("code",[t._v("resource-registry-init")]),t._v(". This service imports the officially supported capabilities during the first startup:")]),t._v(" "),a("ul",[a("li",[a("a",{attrs:{href:"https://github.com/eclipse-slm/slm-dc-docker",target:"_blank",rel:"noopener noreferrer"}},[t._v("Docker"),a("OutboundLink")],1)]),t._v(" "),a("li",[a("a",{attrs:{href:"https://github.com/eclipse-slm/slm-dc-docker-swarm",target:"_blank",rel:"noopener noreferrer"}},[t._v("Docker Swarm"),a("OutboundLink")],1)]),t._v(" "),a("li",[a("a",{attrs:{href:"https://github.com/eclipse-slm/slm-dc-k3s",target:"_blank",rel:"noopener noreferrer"}},[t._v("K3S"),a("OutboundLink")],1)]),t._v(" "),a("li",[a("a",{attrs:{href:"https://github.com/eclipse-slm/slm-dc-k8s",target:"_blank",rel:"noopener noreferrer"}},[t._v("K8S"),a("OutboundLink")],1)])]),t._v(" "),a("h3",{attrs:{id:"add-a-new-deployment-capability"}},[a("a",{staticClass:"header-anchor",attrs:{href:"#add-a-new-deployment-capability"}},[t._v("#")]),t._v(" Add a new Deployment Capability")]),t._v(" "),a("p",[t._v("Additional deployment capabilites can be imported using the endpoint "),a("code",[t._v("POST /resources/capabilites")]),t._v(" of the Resource Registry. A request at the example of the "),a("a",{attrs:{href:"https://github.com/FabOS-AI/fabos-slm-dc-docker",target:"_blank",rel:"noopener noreferrer"}},[t._v("Docker Deployment Capability"),a("OutboundLink")],1),t._v(" looks like this:")]),t._v(" "),a("div",{staticClass:"custom-block warning"},[a("p",{staticClass:"custom-block-title"},[t._v("ATTENTION")]),t._v(" "),a("p",[a("code",[t._v("<<your-slm-host>>")]),t._v(" and "),a("a",{attrs:{href:"http://localhost:8081/docs/usage/api/#authentication",target:"_blank",rel:"noopener noreferrer"}},[a("code",[t._v("<<your-keycloak-token>>")]),a("OutboundLink")],1),t._v(" must be replaced!")])]),t._v(" "),a("div",{staticClass:"language-sh extra-class"},[a("pre",{pre:!0,attrs:{class:"language-sh"}},[a("code",[a("span",{pre:!0,attrs:{class:"token function"}},[t._v("curl")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token parameter variable"}},[t._v("-X")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token string"}},[t._v("'POST'")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("\\")]),t._v("\n  "),a("span",{pre:!0,attrs:{class:"token string"}},[t._v("'http://<<your-slm-host>>:9010/resources/capabilities'")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("\\")]),t._v("\n  "),a("span",{pre:!0,attrs:{class:"token parameter variable"}},[t._v("-H")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token string"}},[t._v("'accept: application/json'")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("\\")]),t._v("\n  "),a("span",{pre:!0,attrs:{class:"token parameter variable"}},[t._v("-H")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token string"}},[t._v("'Realm: fabos'")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("\\")]),t._v("\n  "),a("span",{pre:!0,attrs:{class:"token parameter variable"}},[t._v("-H")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token string"}},[t._v("'Authorization: Bearer <<your-keycloak-token>>'")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("\\")]),t._v("\n  "),a("span",{pre:!0,attrs:{class:"token parameter variable"}},[t._v("-H")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token string"}},[t._v("'Content-Type: application/json'")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("\\")]),t._v("\n  "),a("span",{pre:!0,attrs:{class:"token parameter variable"}},[t._v("-d")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token string"}},[t._v('\'{\n    "uuid": "08c5b8de-5d4a-4116-a73f-1d1f616c7c70",\n    "capabilityClass": "DeploymentCapability",\n    "name": "Docker",\n    "logo": "mdi-docker",\n    "type": [\n      "setup",\n      "deploy"\n    ],\n    "actions": {\n      "install": {\n        "capabilityActionClass": "AwxCapabilityAction",\n        "capabilityActionType": "install",\n        "awxRepo": "https://github.com/eclipse-slm/slm-dc-docker",\n        "awxBranch": "main",\n        "playbook": "install.yml"\n      },\n      "uninstall": {\n        "capabilityActionClass": "AwxCapabilityAction",\n        "capabilityActionType": "uninstall",\n        "awxRepo": "https://github.com/eclipse-slm/slm-dc-docker",\n        "awxBranch": "main",\n        "playbook": "uninstall.yml"\n      },\n      "undeploy": {\n        "capabilityActionClass": "AwxCapabilityAction",\n        "capabilityActionType": "undeploy",\n        "awxRepo": "https://github.com/eclipse-slm/slm-dc-docker",\n        "awxBranch": "main",\n        "playbook": "undeploy.yml"\n      },\n      "deploy": {\n        "capabilityActionClass": "AwxCapabilityAction",\n        "capabilityActionType": "deploy",\n        "awxRepo": "https://github.com/eclipse-slm/slm-dc-docker",\n        "awxBranch": "main",\n        "playbook": "deploy.yml"\n      },\n    },\n    "supportedDeploymentTypes": [\n      "DOCKER_CONTAINER",\n      "DOCKER_COMPOSE"\n    ],\n    "clusterMemberTypes": []\n  }\'')]),t._v("'\n")])])]),a("p",[t._v("If a Deployment Capability is located in a private git repository, username and password / access token must be set for each action:")]),t._v(" "),a("div",{staticClass:"language-json extra-class"},[a("pre",{pre:!0,attrs:{class:"language-json"}},[a("code",[t._v("...\n"),a("span",{pre:!0,attrs:{class:"token property"}},[t._v('"actions"')]),a("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("{")]),t._v("\n    ...\n    "),a("span",{pre:!0,attrs:{class:"token property"}},[t._v('"install"')]),a("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("{")]),t._v("\n        "),a("span",{pre:!0,attrs:{class:"token property"}},[t._v('"capabilityActionClass"')]),a("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token string"}},[t._v('"AwxCapabilityAction"')]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n        "),a("span",{pre:!0,attrs:{class:"token property"}},[t._v('"capabilityActionType"')]),a("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token string"}},[t._v('"install"')]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n        "),a("span",{pre:!0,attrs:{class:"token property"}},[t._v('"awxRepo"')]),a("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token string"}},[t._v('"https://my-private-git-repo/slm-dc-private"')]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n        "),a("span",{pre:!0,attrs:{class:"token property"}},[t._v('"awxBranch"')]),a("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token string"}},[t._v('"main"')]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n        "),a("span",{pre:!0,attrs:{class:"token property"}},[t._v('"playbook"')]),a("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token string"}},[t._v('"install.yml"')]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n        "),a("span",{pre:!0,attrs:{class:"token property"}},[t._v('"username"')]),a("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token string"}},[t._v('"myUser"')]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n        "),a("span",{pre:!0,attrs:{class:"token property"}},[t._v('"password"')]),a("span",{pre:!0,attrs:{class:"token operator"}},[t._v(":")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token string"}},[t._v('"myPassword"')]),t._v("\n      "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("}")]),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v(",")]),t._v("\n      ...\n"),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("}")]),t._v("\n...\n")])])]),a("h3",{attrs:{id:"use-deployment-capability-to-deploy-service-offerings"}},[a("a",{staticClass:"header-anchor",attrs:{href:"#use-deployment-capability-to-deploy-service-offerings"}},[t._v("#")]),t._v(" Use deployment capability to deploy service offerings")]),t._v(" "),a("p",[t._v("To allow a deployment capability to deploy a service offering with a particular deployment type, that deployment type must be defined in the "),a("code",[t._v("supportedDeploymentTypes")]),t._v(" property of the deployment capability. Currently, service offerings can have the following deployment types:")]),t._v(" "),a("ul",[a("li",[t._v("DOCKER_CONTAINER")]),t._v(" "),a("li",[t._v("DOCKER_COMPOSE")]),t._v(" "),a("li",[t._v("KUBERNETES")])])])}),[],!1,null,null,null);a.default=n.exports}}]);