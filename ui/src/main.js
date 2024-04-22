import Vue, {createApp} from 'vue'
import App from './App.vue'
import router from './pages/router'
import store from './store/store'
import '@/plugins/base'
import '@/plugins/chartist'
import '@/plugins/vee-validate'
import 'material-design-icons-iconfont/dist/material-design-icons.css'
import vuetify from './plugins/vuetify'
import i18n from './localisation/i18n'
import setupTokenInterceptor from '@/utils/tokenInterceptor'
import VueKeycloakJs from '@dsb-norge/vue-keycloak-js'
import getEnv from '@/utils/env'
import VueToast from 'vue-toast-notification'
import 'vue-toast-notification/dist/theme-sugar.css'
import enums from 'vue-enums'
import moment from 'moment'

import { Chart, registerables } from 'chart.js'
import {createVuetify} from "vuetify";

let app = createApp(App);

app.config.productionTip = false
app.config.devtools = true

Chart.register(...registerables)

app.prototype.moment = moment

app.use(enums)

app.use(VueToast, {
    position: 'bottom',
    duration: 5000,
    dismissible: true,
})

app.use(VueKeycloakJs, {
    init: {
        onLoad: 'login-required',
        checkLoginIframe: false,
    },
    config: {
        url: getEnv('VUE_APP_KEYCLOAK_URL'),
        realm: getEnv('VUE_APP_KEYCLOAK_REALM'),
        clientId: getEnv('VUE_APP_KEYCLOAK_CLIENT_ID'),
    },
    onReady (keycloak) {
        setupTokenInterceptor()

        // new Vue({
        //     router,
        //     store,
        //     vuetify,
        //     i18n,
        //     keycloak,
        //     render: h => h(App),
        // }).$mount('#app')
    },
})

const theme = {
    primary: '#004263',
    secondary: '#00A0E3',
    accent: '#17A6A6',
    error: '#FF7A5A',
    warning: '#F39430',
    info: '#71BD86',
    success: '#00A0E3',
};

const v = createVuetify({
    lang: {
        t: (key, ...params) => i18n.t(key, params),
    },
    theme: {
        themes: {
            dark: theme,
            light: theme,
        },
    },
});

app.use(router);
app.use(store);
app.use(v);
app.use(i18n);


app.mount('#app');


