package org.eclipse.slm.common.model

class TemplateVariable(

    val key: String,

    val name: String,

    val valueSource: TemplateVariableValueSource,

    val valuePath: String,

)
{

    var value: Any? = null

}
