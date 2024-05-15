import Vue, {toRaw} from 'vue'
import {app} from "@/main";

export default async function () {
    // const keycloak = toRaw(app.config.globalProperties.$keycloak)
    // console.log(keycloak.keycloak)
    console.log(app.config.globalProperties.$keycloak)
    // await app.config.globalProperties.$keycloak.keycloak.updateToken(70)
    // return app.config.globalProperties.$keycloak
}
