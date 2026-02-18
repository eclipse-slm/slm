package org.eclipse.slm.common.consul.model.kv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.Base64

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class KeyValueData(

    /** Number of times this key has successfully been acquired in a lock. */
    @field:JsonProperty("LockIndex")
    val lockIndex: Long? = null,

    /** Full path of the entry. */
    @field:JsonProperty("Key")
    val key: String? = null,

    /** Opaque unsigned integer that can be attached to each entry. */
    @field:JsonProperty("Flags")
    val flags: Long? = null,

    /** "Base64-encoded blob of data. */
    @field:JsonProperty("Value")
    val valueBase64Encoded: String? = null,

    /** Session that owns the lock if the lock is held. */
    @field:JsonProperty("Session")
    val session: String? = null
) {
    fun getDecodedValue(): String? =
        valueBase64Encoded
            ?.let { Base64.getDecoder().decode(it) }
            ?.toString(Charsets.UTF_8)
}