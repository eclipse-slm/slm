import {globals} from "@/main";

export default async function () {
    if(globals.$keycloak?.keycloak?.updateToken !== undefined) {
        return globals.$keycloak.keycloak.updateToken(-1).then((refreshed) => {
            if (refreshed) {
                console.debug('Token refresh successful');
                return refreshed;
            } else {
                console.error('Token refresh failed ');
                this.$keycloak.logoutFn();
            }
        }).catch((error) => {
            console.error('Token refresh failed ', error);
            this.$keycloak.logoutFn();
        });
    }
}
