package org.eclipse.slm.common.keycloak.config.jwt

class MisconfigurationException(msg: String?) : RuntimeException(msg) {
    companion object {
        private const val serialVersionUID = 5887967904749547431L
    }
}