import Vue from 'vue'
import {app} from "@/main";

export default async function () {
    await app.config.globalProperties.$keycloak.keycloak.updateToken(70)
    return app.config.globalProperties.$keycloak
}
