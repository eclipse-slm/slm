package org.eclipse.slm.common.awx.model

data class InventoryDTOApiCreate(
    var name: String,
    var organization: Int
) {
    var kind : String = ""
}
