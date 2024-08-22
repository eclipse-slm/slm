(window.webpackJsonp=window.webpackJsonp||[]).push([[36],{352:function(t,a,e){"use strict";e.r(a);var s=e(15),n=Object(s.a)({},(function(){var t=this,a=t._self._c;return a("ContentSlotsDistributor",{attrs:{"slot-key":t.$parent.slotKey}},[a("h1",{attrs:{id:"installation"}},[a("a",{staticClass:"header-anchor",attrs:{href:"#installation"}},[t._v("#")]),t._v(" Installation")]),t._v(" "),a("h2",{attrs:{id:"prerequisites"}},[a("a",{staticClass:"header-anchor",attrs:{href:"#prerequisites"}},[t._v("#")]),t._v(" Prerequisites")]),t._v(" "),a("ul",[a("li",[t._v("Docker")])]),t._v(" "),a("h2",{attrs:{id:"install"}},[a("a",{staticClass:"header-anchor",attrs:{href:"#install"}},[t._v("#")]),t._v(" Install")]),t._v(" "),a("p",[t._v("Set in your current shell the environment variable "),a("code",[t._v("SLM_HOSTNAME")]),t._v(" to the hostname of the host where the stack will be\nstarted. E.g.:")]),t._v(" "),a("div",{staticClass:"language-sh extra-class"},[a("pre",{pre:!0,attrs:{class:"language-sh"}},[a("code",[a("span",{pre:!0,attrs:{class:"token builtin class-name"}},[t._v("export")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token assign-left variable"}},[t._v("SLM_HOSTNAME")]),a("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v("myhost.local\n")])])]),a("div",{staticClass:"custom-block warning"},[a("p",{staticClass:"custom-block-title"},[t._v("ATTENTION")]),t._v(" "),a("p",[a("strong",[t._v("Use lowercase for the hostname to avoid case problems (e.g. with token authentication)")])])]),t._v(" "),a("p",[t._v("Run the following command to start the SLM installer:")]),t._v(" "),a("div",{staticClass:"language-sh extra-class"},[a("pre",{pre:!0,attrs:{class:"language-sh"}},[a("code",[a("span",{pre:!0,attrs:{class:"token function"}},[t._v("docker")]),t._v(" run "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("\\")]),t._v("\n  "),a("span",{pre:!0,attrs:{class:"token parameter variable"}},[t._v("--rm")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("\\")]),t._v("\n  "),a("span",{pre:!0,attrs:{class:"token parameter variable"}},[t._v("--pull")]),a("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),t._v("always "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("\\")]),t._v("\n  "),a("span",{pre:!0,attrs:{class:"token parameter variable"}},[t._v("--env")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token assign-left variable"}},[t._v("SLM_HOSTNAME")]),a("span",{pre:!0,attrs:{class:"token operator"}},[t._v("=")]),a("span",{pre:!0,attrs:{class:"token variable"}},[t._v("$SLM_HOSTNAME")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("\\")]),t._v("\n  "),a("span",{pre:!0,attrs:{class:"token parameter variable"}},[t._v("--volume")]),t._v(" /var/run/docker.sock:/var/run/docker.sock "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("\\")]),t._v("\n  --add-host "),a("span",{pre:!0,attrs:{class:"token variable"}},[t._v("$SLM_HOSTNAME")]),t._v(":host-gateway "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("\\")]),t._v("\n  ghcr.io/eclipse-slm/slm/installer:1.4.0-SNAPSHOT\n")])])]),a("h2",{attrs:{id:"uninstall"}},[a("a",{staticClass:"header-anchor",attrs:{href:"#uninstall"}},[t._v("#")]),t._v(" Uninstall")]),t._v(" "),a("p",[t._v("Run the following command to start the SLM uninstaller:")]),t._v(" "),a("div",{staticClass:"language-sh extra-class"},[a("pre",{pre:!0,attrs:{class:"language-sh"}},[a("code",[a("span",{pre:!0,attrs:{class:"token function"}},[t._v("docker")]),t._v(" run "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("\\")]),t._v("\n  "),a("span",{pre:!0,attrs:{class:"token parameter variable"}},[t._v("--rm")]),t._v(" "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("\\")]),t._v("\n  "),a("span",{pre:!0,attrs:{class:"token parameter variable"}},[t._v("--volume")]),t._v(" /var/run/docker.sock:/var/run/docker.sock "),a("span",{pre:!0,attrs:{class:"token punctuation"}},[t._v("\\")]),t._v("\n  ghcr.io/eclipse-slm/slm/uninstaller:1.4.0-SNAPSHOT\n")])])]),a("h2",{attrs:{id:"components"}},[a("a",{staticClass:"header-anchor",attrs:{href:"#components"}},[t._v("#")]),t._v(" Components")]),t._v(" "),a("h3",{attrs:{id:"ports"}},[a("a",{staticClass:"header-anchor",attrs:{href:"#ports"}},[t._v("#")]),t._v(" Ports")]),t._v(" "),a("p",[t._v("The different components of the stack can be reached under the following ports:")]),t._v(" "),a("ul",[a("li",[t._v("AWX: "),a("a",{attrs:{href:""}},[t._v("http://myhost.local:80")])]),t._v(" "),a("li",[t._v("Consul: "),a("a",{attrs:{href:""}},[t._v("http://myhost.local:8500")])]),t._v(" "),a("li",[t._v("Keycloak: "),a("a",{attrs:{href:""}},[t._v("http://myhost.local:7080")])]),t._v(" "),a("li",[t._v("Resource Registry: "),a("a",{attrs:{href:""}},[t._v("http://myhost.local:9010")])]),t._v(" "),a("li",[t._v("Service Registry: "),a("a",{attrs:{href:""}},[t._v("http://myhost.local:9020")])]),t._v(" "),a("li",[t._v("Vault: "),a("a",{attrs:{href:""}},[t._v("http://myhost.local:8200")])]),t._v(" "),a("li",[t._v("UI: "),a("a",{attrs:{href:""}},[t._v("http://myhost.local:8080")])])]),t._v(" "),a("div",{staticClass:"custom-block warning"},[a("p",{staticClass:"custom-block-title"},[t._v("ATTENTION")]),t._v(" "),a("p",[a("strong",[t._v("You need to replace "),a("code",[t._v("myhost.local")]),t._v(" with the hostname of the host where you have installated the Service Lifecycle Management (see section "),a("a",{attrs:{href:"#start"}},[t._v("Start")]),t._v(").")])])]),t._v(" "),a("h3",{attrs:{id:"get-configuration"}},[a("a",{staticClass:"header-anchor",attrs:{href:"#get-configuration"}},[t._v("#")]),t._v(" Get configuration")]),t._v(" "),a("p",[t._v("Most of the settings and credentials are created automatically during first start of the stack. If you want to access the different components of the Service Lifecycle Management stack you need to export the configuration. Wait until the stack is fully started and all init containers have stopped. Then run the config exporter container:")]),t._v(" "),a("div",{staticClass:"language- extra-class"},[a("pre",{pre:!0,attrs:{class:"language-text"}},[a("code",[t._v("docker-compose up --force-recreate config-exporter\n")])])]),a("p",[t._v("It will generate by default a sub-directory "),a("code",[t._v("config/_conf_generated")]),t._v(" relative to your docker-compose.yml file containing\nthe configuration of the setup stack ("),a("code",[t._v("slm-config.yml")]),t._v("). If you want another target directory edit in file "),a("code",[t._v("config-exporter.yml")]),t._v("\nthe host path of this volume:")]),t._v(" "),a("div",{staticClass:"language- extra-class"},[a("pre",{pre:!0,attrs:{class:"language-text"}},[a("code",[t._v("- ./config:/project\n")])])]),a("h2",{attrs:{id:"known-issues"}},[a("a",{staticClass:"header-anchor",attrs:{href:"#known-issues"}},[t._v("#")]),t._v(" Known Issues")]),t._v(" "),a("h3",{attrs:{id:"default-deployment-capabilities-missing"}},[a("a",{staticClass:"header-anchor",attrs:{href:"#default-deployment-capabilities-missing"}},[t._v("#")]),t._v(" Default Deployment Capabilities missing")]),t._v(" "),a("p",[t._v("By default the SLM setup routine adds deployment capabilities for")]),t._v(" "),a("ul",[a("li",[t._v("docker")]),t._v(" "),a("li",[t._v("docker-swarm")]),t._v(" "),a("li",[t._v("k3s")])]),t._v(" "),a("p",[t._v("which are added during the initial startup. In rare cases this adding process fails and consequently no deployment\ncapabilities are available in the UI:")]),t._v(" "),a("figure",[a("img",{attrs:{src:t.$withBase("/img/figures/installation/known-issues-missing-dcs-dc-button-disabled.png")}}),t._v(" "),a("figcaption",[t._v("Deployment Capability Button disabled because of missing single host deployment capabilities")])]),t._v(" "),a("figure",[a("img",{attrs:{src:t.$withBase("/img/figures/installation/known-issues-missing-dcs-cluster-button-disabled.png")}}),t._v(" "),a("figcaption",[t._v("Cluster Button disabled because of missing multi host deployment capabilities")])]),t._v(" "),a("p",[t._v("To fix this start the container "),a("code",[t._v("resource-registry-init")]),t._v(" again by running:")]),t._v(" "),a("div",{staticClass:"language- extra-class"},[a("pre",{pre:!0,attrs:{class:"language-text"}},[a("code",[t._v("docker-compose up -d resource-registry-init\n")])])]),a("p",[t._v("After the container has started and has added the deployment capabilities and it will stop by itself. The capabilities (single\nhost and cluster) should be available after reloading the ui:")]),t._v(" "),a("figure",[a("img",{attrs:{src:t.$withBase("/img/figures/installation/known-issues-missing-dcs-dc-button-enabled.png")}}),t._v(" "),a("figcaption",[t._v("Deployment Capability Button enabled")])]),t._v(" "),a("figure",[a("img",{attrs:{src:t.$withBase("/img/figures/installation/known-issues-missing-dcs-cluster-button-enabled.png")}}),t._v(" "),a("figcaption",[t._v("Cluster Create Button enabled")])]),t._v(" "),a("figure",[a("img",{attrs:{src:t.$withBase("/img/figures/installation/known-issues-missing-dcs-cluster-types-available.png")}}),t._v(" "),a("figcaption",[t._v("Default Cluster Types available in selection form")])])])}),[],!1,null,null,null);a.default=n.exports}}]);