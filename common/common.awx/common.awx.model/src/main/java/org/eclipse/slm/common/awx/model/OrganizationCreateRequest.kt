package org.eclipse.slm.common.awx.model

data class OrganizationCreateRequest(
        var name: String,
        var description: String="",
        var max_hosts: Int=0,
        var custom_virtualenv: String="",
) {
        constructor(name: String) : this(name = name, description = "", max_hosts = 0, custom_virtualenv = "") {}
}
