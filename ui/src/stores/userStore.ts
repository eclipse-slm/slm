import ApiState from '@/api/apiState'
import {defineStore} from "pinia";
import {globals} from "@/main";

interface KeycloakLike {
    authenticated?: boolean,
    loadUserInfo?: () => Promise<any>,
}

const KEYCLOAK_WAIT_INTERVAL_MS = 100;
const KEYCLOAK_WAIT_TIMEOUT_MS = 10000;

function sleep (ms: number) {
    return new Promise((resolve) => setTimeout(resolve, ms));
}

async function waitForKeycloak (timeoutMs = KEYCLOAK_WAIT_TIMEOUT_MS): Promise<KeycloakLike | undefined> {
    const startedAt = Date.now();

    while ((Date.now() - startedAt) < timeoutMs) {
        const keycloak = globals.$keycloak?.keycloak as KeycloakLike | undefined;

        if (keycloak?.loadUserInfo !== undefined) {
            return keycloak;
        }

        await sleep(KEYCLOAK_WAIT_INTERVAL_MS);
    }

    return undefined;
}

interface UserStoreState {
    apiState: number,
    userInfo: null | any,
}

export const useUserStore = defineStore('userStore', {
    persist: true,

    state:():UserStoreState => ({
       apiState: ApiState.INIT,
       userInfo: null
    }),

    getters: {
        userId: (state) => {
            return state.userInfo?.sub
        },
        fullPathUserGroupId: (state) => {
            return `/users/${state.userInfo?.sub}`
        },
        userName: (state) => {
            return state.userInfo?.preferred_username
        },
        userRoles() {
            const roles = globals.$keycloak?.realmAccess?.roles;
            if(roles === undefined){
                return [];
            }

            return roles;
        },
        isUserDeveloper(): boolean{
            let isDeveloper = false
            this.userGroups.forEach(userGroup => {
                if (userGroup.startsWith('/vendor')) {
                    isDeveloper = true
                }
            })
            if(this !== undefined){
            }

            return isDeveloper
        },
        userGroups(): any[]{
            const groups = globals.$keycloak?.tokenParsed?.groups
            if (groups === undefined) {
                return []
            } else {
                return groups
            }
        },
    },
    actions: {
        async getUserDetails () {
            const keycloak = await waitForKeycloak();

            if (keycloak === undefined) {
                console.warn('Keycloak was not initialized in time. userInfo will be cleared.');
                this.userInfo = null;
                return;
            }

            if (!keycloak.authenticated) {
                this.userInfo = null;
                return;
            }

            try {
                this.userInfo = await keycloak.loadUserInfo?.() ?? null;
            } catch (error) {
                this.userInfo = null;
                throw error;
            }
        },

        async updateStore () {
            if (this.apiState === ApiState.INIT) {
                this.apiState = ApiState.LOADING;
            } else {
                this.apiState = ApiState.UPDATING;
            }

            return Promise.all([
                this.getUserDetails(),
            ]).then(() => {
                this.apiState = ApiState.LOADED;
                console.log("userStore updated")
            }).catch((e) => {
                this.apiState = ApiState.ERROR;
                console.log("Failed to update userStore: ", e)
            });
        },
    },
});
