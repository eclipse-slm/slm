package org.eclipse.slm.common.awx.model

enum class CredentialTypeName(val prettyName: String) {
    AMAZON_WEB_SERVICES("Amazon Web Services"),
    ANSIBLE_GALAXY("Ansible Galaxy/Automation Hub API Token"),
    ANSIBLE_TOWER("Ansible Tower"),
    CYBERARK_AIM_CENTRAL_CREDENTIAL_PROVIDER_LOOKUP("CyberArk AIM Central Credential Provider Lookup"),
    CYBERARK_CONJUR_SECRET_LOOKUP("CyberArk Conjur Secret Lookup"),
    GITHUB_PERSONAL_ACCESS_TOKEN("GitHub Personal Access Token"),
    GITLAB_PERSONAL_ACCESS_TOKEN("GitLab Personal Access Token"),
    GOOGLE_COMPUTE_ENGINE("Google Compute Engine"),
    HASHICORP_VAULT_SECRET_LOOKUP("HashiCorp Vault Secret Lookup"),
    HASHICORP_VAULT_SIGNED_SSH("HashiCorp Vault Signed SSH"),
    INSIGHTS("Insights"),
    MACHINE("Machine"),
    MICROSOFT_AZURE_KEY_VAULT("Microsoft Azure Key Vault"),
    MICROSOFT_AZURE_RESOURCE_MANAGER("Microsoft Azure Resource Manager"),
    NETWORK("Network"),
    OPEN_STACK("OpenStack"),
    OPENSHIFT_KUBERNETES_API_BEARER_TOKEN("OpenShift or Kubernetes API Bearer Token"),
    RED_HAT_CLOUD_FORMS("Red Hat CloudForms"),
    RED_HAT_SATELLITE_6("Red Hat Satellite 6"),
    RED_HAT_VIRTUALIZATION("Red Hat Virtualization"),
    SOURCE_CONTROL("Source Control"),
    VAULT("Vault"),
    VMWARE_VCENTER("VMware vCenter")
}
