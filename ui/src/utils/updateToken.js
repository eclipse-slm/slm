import {app} from "@/main";

export default async function () {
    if(app.config.globalProperties.$keycloak?.keycloak?.updateToken !== undefined) {
        await app.config.globalProperties.$keycloak.keycloak.updateToken(70);
    }
    return app.config.globalProperties.$keycloak;
}
