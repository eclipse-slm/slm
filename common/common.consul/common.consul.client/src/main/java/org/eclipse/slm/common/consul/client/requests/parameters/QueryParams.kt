package org.eclipse.slm.common.consul.client.requests.parameters

import org.eclipse.slm.common.consul.client.requests.ConsistencyMode
import org.eclipse.slm.common.consul.client.utils.Utils
import java.util.*

class QueryParams private constructor(
    val datacenter: String?,
    val consistencyMode: ConsistencyMode,
    val waitTime: Long,
    val index: Long,
    val near: String? = null
) : UrlParameters {
    class Builder private constructor() {
        private var datacenter: String? = null
        private var consistencyMode: ConsistencyMode
        private var waitTime: Long
        private var index: Long
        private var near: String?
        fun setConsistencyMode(consistencyMode: ConsistencyMode): Builder {
            this.consistencyMode = consistencyMode
            return this
        }

        fun setDatacenter(datacenter: String?): Builder {
            this.datacenter = datacenter
            return this
        }

        fun setWaitTime(waitTime: Long): Builder {
            this.waitTime = waitTime
            return this
        }

        fun setIndex(index: Long): Builder {
            this.index = index
            return this
        }

        fun setNear(near: String?): Builder {
            this.near = near
            return this
        }

        fun build(): QueryParams {
            return QueryParams(datacenter, consistencyMode, waitTime, index, near)
        }

        companion object {
            fun builder(): Builder {
                return Builder()
            }
        }

        init {
            consistencyMode = ConsistencyMode.DEFAULT
            waitTime = -1
            index = -1
            near = null
        }
    }

    constructor(datacenter: String?) : this(datacenter, ConsistencyMode.DEFAULT, -1, -1) {}
    constructor(consistencyMode: ConsistencyMode) : this(null, consistencyMode, -1, -1) {}
    constructor(datacenter: String?, consistencyMode: ConsistencyMode) : this(datacenter, consistencyMode, -1, -1) {}
    constructor(waitTime: Long, index: Long) : this(null, ConsistencyMode.DEFAULT, waitTime, index) {}
    constructor(datacenter: String?, waitTime: Long, index: Long) : this(
        datacenter,
        ConsistencyMode.DEFAULT,
        waitTime,
        index,
        null
    ) {
    }

    override fun toUrlParameters(): List<String?>? {
        val params: MutableList<String?> = ArrayList()

        // add basic params
        if (datacenter != null) {
            params.add("dc=" + Utils.encodeValue(datacenter))
        }
        if (consistencyMode !== ConsistencyMode.DEFAULT) {
            params.add(consistencyMode.name.lowercase(Locale.getDefault()))
        }
        if (waitTime != -1L) {
            params.add("wait=" + Utils.toSecondsString(waitTime))
        }
        if (index != -1L) {
            params.add("index=" + java.lang.Long.toUnsignedString(index))
        }
        if (near != null) {
            params.add("near=" + Utils.encodeValue(near))
        }
        return params
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is QueryParams) {
            return false
        }
        val that = o
        return waitTime == that.waitTime && index == that.index &&
                datacenter == that.datacenter && consistencyMode === that.consistencyMode &&
                near == that.near
    }

    override fun hashCode(): Int {
        return Objects.hash(datacenter, consistencyMode, waitTime, index, near)
    }

    companion object {
        val DEFAULT = QueryParams(ConsistencyMode.DEFAULT)
    }
}
