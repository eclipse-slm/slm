import axios from 'axios';
import {globals} from "@/main";
import {useEnvStore} from "@/stores/environmentStore";
import updateToken from "@/utils/updateToken";

export default function setup () {
    const envStore = useEnvStore();

    axios.interceptors.request.use( async (config) => {
        if (globals.$keycloak.authenticated) {
            // Refresh token before call
            return new Promise((resolve, reject) => {
                updateToken().then(() => {
                        config.headers.Authorization = `Bearer ${globals.$keycloak.token}`;
                        config.headers.RefreshToken = globals.$keycloak.refreshToken;
                        config.headers.Realm = envStore.keycloakRealm;

                        resolve (config);
                    });
            });
        }
    }, error => {
        return Promise.reject(error);
    });

    axios.interceptors.response.use(
        (res) => {
            return res;
        },
        async (err) => {
            const originalConfig = err.config;

            if (originalConfig.url.indexOf('/auth') === -1 && err.response) {
                // Access Token was expired
                if (err.response.status === 401 && !originalConfig._retry) {
                    console.log('Try to refresh token');
                    originalConfig._retry = true;

                    try {
                        const refreshed = await updateToken();
                        if (refreshed) {
                            console.log('Token refreshed' + refreshed);
                        } else {
                            console.warn('Token not refreshed, valid for ' +
                                Math.round(globals.$keycloak.tokenParsed.exp + globals.$keycloak.timeSkew - new Date().getTime() / 1000) + ' seconds');
                        }
                        console.log('return original config');
                        return axios(originalConfig);
                    } catch (_error) {
                        console.log(_error);
                        return Promise.reject(_error);
                    }
                }
            }

            return Promise.reject(err)
        },
    )
}
