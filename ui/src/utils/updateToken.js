import {app} from "@/main";
import {toRaw} from "vue";

export default async function () {
    await app.config.globalProperties.$keycloak.keycloak.updateToken(70)
    return app.config.globalProperties.$keycloak
}
