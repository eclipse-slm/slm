package org.eclipse.slm.resource_management.model.resource


class ExecutionEnvironment {
    var image: String = ""
    var pull: ExecutionEnvironmentPull = ExecutionEnvironmentPull.None
    var description: String = ""
    var registryCredential: RegistryCredentials? = null
}