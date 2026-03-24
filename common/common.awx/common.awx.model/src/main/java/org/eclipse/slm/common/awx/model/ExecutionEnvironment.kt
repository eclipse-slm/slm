package org.eclipse.slm.common.awx.model

import java.util.*

class ExecutionEnvironment(
    var id: Int,
    var type: String,
    var url: String,
    var related: Map<String, String>,
    var summary_fields: Map<String, Any>,
    var created: Date,
    var modified: Date,
    var name: String,
    var description: String,
    val organization: Int,
    var image: String,
    var managed: String,
    val credential: Int,
    var pull: String

) {
}