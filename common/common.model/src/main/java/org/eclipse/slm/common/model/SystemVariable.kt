package org.eclipse.slm.common.model

class SystemVariable(

    val key: String,

    val name: String,

    val valueSource: SystemVariableValueSource,

    val valuePath: String,

    )
{

    var value: Any? = null

    override fun toString(): String {
        return "[key='$key', name='$name', valueSource=$valueSource, valuePath='$valuePath', value=$value]"
    }

}
