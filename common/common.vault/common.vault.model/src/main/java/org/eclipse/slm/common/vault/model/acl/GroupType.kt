package org.eclipse.slm.common.vault.model.acl

enum class GroupType(private val value: String) {
    INTERNAL("internal"),
    EXTERNAL("external");

    override fun toString(): String {
        return value
    }
}