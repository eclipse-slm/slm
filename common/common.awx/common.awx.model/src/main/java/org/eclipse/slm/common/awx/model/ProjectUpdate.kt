package org.eclipse.slm.common.awx.model

import java.util.*

data class ProjectUpdate(
    var id: Int?,
    var type: String?,
    var url: String?,
    var related: Map<String?, String>?,
    var summary_fields: Map<String?, Any>?,
    var created: Date?,
    var modified: Date?,
    var name: String?,
    var description: String?,
    var local_path: String?,
    var scm_type: String?,
    var scm_url: String?,
    var scm_branch: String?,
    var scm_refspec: String?,
    var scm_clean: Boolean? = false,
    var scm_delete_on_update: Boolean? = false,
    var timeout: Int?,
    var scm_revision: String?,
    var last_job_run: Date?,
    var last_job_failed: Boolean?,
    var status: String?,
    var organization: Int?,
    var scm_update_on_launch: Boolean? = false,
    var scm_update_cache_timeout: Int?,
    var allow_override: Boolean?,
    var last_update_failed: Boolean?,
    var last_updated: Date?
)
