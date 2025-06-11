import ApiState from '@/api/apiState'
import {defineStore} from "pinia";
import {globals} from "@/main";

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
        userName: (state) => {
            return state.userInfo.preferred_username
        },
        userRoles(state) {
            const roles = globals.$keycloak?.realmAccess?.roles;
            if(roles === undefined){
                return [];
            }

            return roles;
        },
        isUserDeveloper(state): boolean{
            let isDeveloper = false
            this.userGroups.forEach(userGroup => {
                if (userGroup.startsWith('vendor')) {
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
            if (globals.$keycloak?.keycloak != undefined) {
                if (globals.$keycloak.keycloak.authenticated) {
                    await globals.$keycloak?.keycloak.loadUserInfo().then(userInfo => {
                        this.userInfo = userInfo;
                    })
                }
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
