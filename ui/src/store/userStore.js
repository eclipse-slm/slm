import ApiState from '@/api/apiState'
import Vue from 'vue'

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
            return state.userInfo.sub
        },

        userName: (state) => {
            return state.userInfo.preferred_username
        },

        userInfo: (state) => {
            return state.userInfo
        },

        userRoles: () => {
            var roles = Vue.prototype.$keycloak.realmAccess.roles
            return roles
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
            const groups = Vue.prototype.$keycloak.tokenParsed.groups

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

            await Vue.prototype.$keycloak.keycloak.loadUserInfo().then(userInfo => {
                 context.commit('SET_USERINFO', userInfo)
            })

            context.commit('SET_API_STATE_USER', ApiState.LOADED)
        },
    },

}
