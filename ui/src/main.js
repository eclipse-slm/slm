import Vue from 'vue'
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

Vue.config.productionTip = false
Vue.config.devtools = true

Chart.register(...registerables)

Vue.prototype.moment = moment

Vue.use(enums)

Vue.use(VueToast, {
    position: 'bottom',
    duration: 5000,
    dismissible: true,
})

Vue.use(VueKeycloakJs, {
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

        new Vue({
            router,
            store,
            vuetify,
            i18n,
            keycloak,
            render: h => h(App),
        }).$mount('#app')
    },
})
