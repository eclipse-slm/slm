import {createApp, configureCompat } from 'vue'
import App from './App.vue'
import router from './pages/router'
import {store} from './store/store'
// import '@/plugins/base'
// import '@/plugins/chartist'
import 'chartist/dist/chartist.min.css'
import '@/plugins/vee-validate'
import 'material-design-icons-iconfont/dist/material-design-icons.css'
import {i18n} from './localisation/i18n'
import setupTokenInterceptor from '@/utils/tokenInterceptor'
import VueKeycloakJs from '@dsb-norge/vue-keycloak-js'
import getEnv from '@/utils/env'
import VueToast from 'vue-toast-notification'
import 'vue-toast-notification/dist/theme-sugar.css'
import enums from 'vue-enums'
// import moment from 'moment'

import { Chart, registerables } from 'chart.js'
import {createVuetify} from "vuetify";
import VueAxios from "vue-axios";
import axios from "axios";
import cors from "cors";
import NotificationServiceWebsocketClient from "@/api/notification-service/notificationServiceWebsocketClient";


configureCompat({
    MODE:3,
    CONFIG_PRODUCTION_TIP: false,
    GLOBAL_PROTOTYPE: true,
    GLOBAL_OBSERVABLE: true
});

export let app = createApp(App);

const requireComponent = require.context(
    '@/components/base', true, /\.vue$/,
)

import upperFirst from 'lodash/upperFirst'
import camelCase from 'lodash/camelCase'

import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";


requireComponent.keys().forEach(fileName => {
    const componentConfig = requireComponent(fileName)

    const componentName = upperFirst(
        camelCase(fileName.replace(/^\.\//, '').replace(/\.\w+$/, '')),
    )

    app.component(`Base${componentName}`, componentConfig.default || componentConfig)
})

app.component('NoItemAvailableNote', NoItemAvailableNote);

console.log(requireComponent.keys())

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
        keycloak.updateToken(70).then((r) => console.log(r))
        setupTokenInterceptor()
        NotificationServiceWebsocketClient.connect();
        app.config.globalProperties.$store.dispatch('getUserDetails')
        app.config.globalProperties.$store.dispatch('updateCatalogStore')
        app.config.globalProperties.$store.dispatch('initServiceStore')
        app.config.globalProperties.$store.dispatch('getVirtualResourceProviders')
        app.config.globalProperties.$store.dispatch('getServiceHosters')
        app.config.globalProperties.$store.dispatch('getServiceInstanceGroups')
        app.config.globalProperties.$store.dispatch('getDeploymentCapabilities')
        app.config.globalProperties.$store.dispatch('getResourcesFromBackend')
        app.config.globalProperties.$store.dispatch('getResourceAASFromBackend')
        app.config.globalProperties.$store.dispatch('getLocations')
        app.config.globalProperties.$store.dispatch('getProfiler')
        app.config.globalProperties.$store.dispatch('getCluster')
        app.config.globalProperties.$store.dispatch('getNotifications')
        app.config.globalProperties.$store.dispatch('getClusterTypes')
    },
})

Chart.register(...registerables)

// app.prototype.moment = moment

app.use(enums)

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

app.use(VueAxios, axios)
app.use(cors)

app.use(router);
app.use(store);
app.use(v);
app.use(i18n);

app.use(require('vue-chartist'));


app.use(VueToast,{
    position: 'bottom',
    duration: 5000,
    dismissible: true,}
);

app.mount('#app');
