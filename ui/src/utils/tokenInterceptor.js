import axios from 'axios'
import Vue from 'vue'
import getEnv from '@/utils/env'

export default function setup () {
    axios.interceptors.request.use(config => {
        if (Vue.prototype.$keycloak.authenticated) {
            config.headers.Authorization = `Bearer ${Vue.prototype.$keycloak.token}`
            config.headers.RefreshToken = Vue.prototype.$keycloak.refreshToken
            config.headers.Realm = getEnv('VUE_APP_KEYCLOAK_REALM')
        }
        return config
    }, error => {
        return Promise.reject(error)
    })

    axios.interceptors.response.use(
        (res) => {
            return res
        },
        async (err) => {
            const originalConfig = err.config

            if (originalConfig.url.indexOf('/auth') === -1 && err.response) {
                // Access Token was expired
                if (err.response.status === 401 && !originalConfig._retry) {
                    console.log('Try to refresh token')
                    originalConfig._retry = true

                    try {
                        const refreshed = await Vue.prototype.$keycloak.keycloak.updateToken(60)
                        if (refreshed) {
                            console.log('Token refreshed' + refreshed)
                        } else {
                            console.warn('Token not refreshed, valid for ' +
                                Math.round(Vue.prototype.$keycloak.tokenParsed.exp + Vue.prototype.$keycloak.keycloak.timeSkew - new Date().getTime() / 1000) + ' seconds')
                        }
                        console.log('return original config')
                        return axios(originalConfig)
                    } catch (_error) {
                        console.log(_error)
                        return Promise.reject(_error)
                    }
                }
            }

            return Promise.reject(err)
        },
    )
}
