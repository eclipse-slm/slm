package org.eclipse.slm.common.awx.model

import java.util.*

data class Project (
    var id: Int?,
    var name: String?,
    var description: String?,
    var scm_type: String?,
    var scm_url: String?,
    var scm_branch: String?,
    var type: String?,
    var url: String?,
    var related: Map<String?, String>?,
    var summary_fields: Map<String?, Any>?,
    var created: Date?,
    var modified: Date?,
    var local_path: String?,
    var scm_refspec: String?,
    var scm_clean: Boolean? = false,
    var scm_delete_on_update: Boolean? = false,
    var credential: Int?,
    var timeout: Int?,
    var scm_revision: String?,
    var last_job_run: Date?,
    var last_job_failed: Boolean?,
    var next_job_run: String?,
    var status: String?,
    var organization: Int?,
    var scm_update_on_launch: Boolean? = false,
    var scm_update_cache_timeout: Int?,
    var allow_override: Boolean?,
    var custom_virtualenv: String?,
    var last_update_failed: Boolean?,
    var last_updated: Date?
)
