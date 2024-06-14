import ApiState from '@/api/apiState'
import {app} from "@/main";

export default {
    state: {
        apiStateUser: ApiState.INIT,

        userInfo: null,
    },

    getters: {
        apiStateUser: (state) => {
            return state.apiStateUser
        },

        userId: (state) => {
            return state.userInfo?.sub
        },

        userName: (state) => {
            return state.userInfo.preferred_username
        },

        userInfo: (state) => {
            return state.userInfo
        },

        userRoles: () => {

            const roles = app.config.globalProperties.$keycloak?.realmAccess?.roles
            if(roles === undefined){
                return [];
            }

            return roles;
        },

        isUserDeveloper: (state, getters) => {
            let isDeveloper = false
            getters.userGroups.forEach(userGroup => {
                if (userGroup.startsWith('vendor')) {
                    isDeveloper = true
                }
            })

            return isDeveloper
        },

        userGroups: () => {

            const groups = app.config.globalProperties.$keycloak?.tokenParsed?.groups
            console.log(groups)
            if (groups === undefined) {
                return []
            } else {
                return groups
            }
        },
    },

    mutations: {
        SET_API_STATE_USER (state, apiState) {
            state.apiStateUser = apiState
        },

        SET_USERINFO (state, userInfo) {
            state.userInfo = userInfo
        },
    },

    actions: {
        async getUserDetails (context) {
            context.commit('SET_API_STATE_USER', ApiState.LOADING)

            // console.log('MyLOG', app.config.globalProperties.$keycloak.ready);

            if (app.config.globalProperties.$keycloak.keycloak.authenticated){
                await app.config.globalProperties.$keycloak.keycloak.loadUserInfo().then(userInfo => {
                    context.commit('SET_USERINFO', userInfo)
                })
            }

            context.commit('SET_API_STATE_USER', ApiState.LOADED)
        },
    },

}
