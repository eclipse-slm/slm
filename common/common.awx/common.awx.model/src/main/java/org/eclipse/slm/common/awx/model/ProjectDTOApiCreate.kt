package org.eclipse.slm.common.awx.model

data class ProjectDTOApiCreate(
    var name: String,
    var description: String = "",
    var scm_url: String,
    var scm_branch: String,
    var scm_type: String = "git",
    var scm_update_on_launch: Boolean = false,
    var scm_clean: Boolean = false,
    var scm_delete_on_update: Boolean = false,
) {
    constructor(scm_url: String, scm_branch: String) : this(
        name = scm_url.split("/").last() + " - $scm_branch",
        description = "$scm_url - $scm_branch",
        scm_url = scm_url,
        scm_branch = scm_branch
    ) {}

    constructor(
        scm_url: String,
        scm_branch: String,
        credential: Credential
    ) : this(
        name = scm_url.split("/").last() + " - $scm_branch - ${credential.inputs["username"]}",
        description = "$scm_url - $scm_branch - ${credential.inputs["username"]}",
        scm_url = scm_url,
        scm_branch = scm_branch
    ) {
        this.credential = credential.id
    }

    constructor(
        name: String,
        description: String,
        scm_url: String,
        scm_branch: String
    ) : this(
        name,
        description,
        scm_url,
        scm_branch,
        "git",
        false,
        false,
        false) {}

    var organization : Int? = null
    var credential: Int? = null
}
