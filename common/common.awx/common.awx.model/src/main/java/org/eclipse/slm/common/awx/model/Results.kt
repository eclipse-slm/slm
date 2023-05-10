package org.eclipse.slm.common.awx.model

data class Results<T> (
    var count: Int,
    var next: String? = null,
    var previous: String? = null,
    var results: Collection<T>
)
