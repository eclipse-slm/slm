package org.eclipse.slm.common.awx.model

data class JobTemplateDTOApiCreate(

    val organization: Int,

    val project: Int,

    val inventory: Int,

    val name: String,

    val playbook: String,

) {
    var ask_variables_on_launch: Boolean = true

    var allow_simultaneous: Boolean = true
}
