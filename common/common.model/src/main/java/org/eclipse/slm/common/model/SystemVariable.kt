package org.eclipse.slm.common.model

class SystemVariable(

    val key: String,

    val name: String,

    val valueSource: SystemVariableValueSource,

    val valuePath: String,

    )
{

    var value: Any? = null

}
