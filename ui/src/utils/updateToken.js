import Vue from 'vue'

export default async function () {
    await Vue.prototype.$keycloak.keycloak.updateToken(70)
    return Vue.prototype.$keycloak.token
}
