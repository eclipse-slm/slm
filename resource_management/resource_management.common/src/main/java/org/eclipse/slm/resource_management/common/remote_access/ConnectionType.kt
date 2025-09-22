package org.eclipse.slm.resource_management.common.remote_access

enum class ConnectionType(var prettyName: String, var remoteAccess: Boolean, var defaultPort: Int? = null) {
    ssh("ssh", true, 22),
    WinSsh("win-ssh", true, 22),
    WinRM("WinRM", true, 5985),
    http("http",false),
    tcp("tcp",false)
}
