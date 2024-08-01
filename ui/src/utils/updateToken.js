import {app} from "@/main";
import {toRaw} from "vue";

export default async function () {
    if(app.config.globalProperties.$keycloak?.keycloak?.updateToken !== undefined) {
        await app.config.globalProperties.$keycloak.keycloak.updateToken(70);
    }
    return app.config.globalProperties.$keycloak;
}
