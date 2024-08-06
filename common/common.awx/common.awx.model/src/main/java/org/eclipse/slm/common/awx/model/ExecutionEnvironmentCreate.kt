package org.eclipse.slm.common.awx.model

class ExecutionEnvironmentCreate (

    var name: String,
    var description: String,
    val organization: Int,
    var image: String,
    var managed: String,
    val credential: Int?,
    var pull: String

) {
}