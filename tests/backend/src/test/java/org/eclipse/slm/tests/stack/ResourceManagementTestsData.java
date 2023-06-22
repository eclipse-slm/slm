package org.eclipse.slm.tests.stack;

public class ResourceManagementTestsData {

    public final static String DUMMY_DEPLOYMENT_CAPABILITY_JSON = """
            {
                "id": "40f2a3e0-c4c7-4abb-8385-cfc4934661d0",
                "capabilityClass": "DeploymentCapability",
                "name": "Dummy",
                "logo": "mdi-docker",
                "type": [
                  "SETUP",
                  "DEPLOY"
                ],
                "actions": {
                  "INSTALL": {
                    "capabilityActionClass": "AwxCapabilityAction",
                    "capabilityActionType": "INSTALL",
                    "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-dummy.git",
                    "awxBranch": "main",
                    "playbook": "install.yml"
                  },
                  "UNINSTALL": {
                    "capabilityActionClass": "AwxCapabilityAction",
                    "capabilityActionType": "UNINSTALL",
                    "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-dummy.git",
                    "awxBranch": "main",
                    "playbook": "uninstall.yml"
                  },
                  "DEPLOY": {
                    "capabilityActionClass": "AwxCapabilityAction",
                    "capabilityActionType": "DEPLOY",
                    "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-dummy.git",
                    "awxBranch": "main",
                    "playbook": "deploy.yml"
                  },
                  "UNDEPLOY": {
                    "capabilityActionClass": "AwxCapabilityAction",
                    "capabilityActionType": "UNDEPLOY",
                    "awxRepo": "https://github.com/FabOS-AI/fabos-slm-dc-dummy.git",
                    "awxBranch": "main",
                    "playbook": "undeploy.yml"
                  }
                }
            }
            """;

}
