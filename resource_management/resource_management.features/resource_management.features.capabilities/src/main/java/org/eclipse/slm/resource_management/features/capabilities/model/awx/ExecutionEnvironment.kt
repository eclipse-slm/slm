package org.eclipse.slm.resource_management.features.capabilities.model.awx

import org.eclipse.slm.resource_management.common.model.RegistryCredentials


class ExecutionEnvironment {
    var image: String = ""
    var pull: ExecutionEnvironmentPull = ExecutionEnvironmentPull.None
    var description: String = ""
    var registryCredential: RegistryCredentials? = null
}