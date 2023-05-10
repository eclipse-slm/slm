package org.eclipse.slm.common.consul.model.keyvalue

class KeyValueReturnValue{
    var createIndex: Int = 0
    var modifyIndex: Int = 0
    var lockIndex: Int = 0
    var key: String = ""
    var flags: Int = 0
    var value: String = ""
    var session: String? = null

    public constructor(){}
}

