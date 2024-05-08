import axios from 'axios'
import getEnv from '@/utils/env'
import {app} from "@/main";

export default function setup () {
    axios.interceptors.request.use(config => {

        if (app.config.globalProperties.$keycloak.authenticated) {
            config.headers.Authorization = `Bearer ${app.config.globalProperties.$keycloak.token}`
            config.headers.RefreshToken = app.config.globalProperties.$keycloak.refreshToken
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
                        const refreshed = await app.config.globalProperties.$keycloak.keycloak.updateToken(60)
                        if (refreshed) {
                            console.log('Token refreshed' + refreshed)
                        } else {
                            console.warn('Token not refreshed, valid for ' +
                                Math.round(app.config.globalProperties.$keycloak.tokenParsed.exp + app.config.globalProperties.$keycloak.timeSkew - new Date().getTime() / 1000) + ' seconds')
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
