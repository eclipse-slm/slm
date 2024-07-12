import {createApp, configureCompat } from 'vue'
import App from './App.vue'
import router from './pages/router'
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
import {createVuetify, ThemeDefinition} from "vuetify";
import VueAxios from "vue-axios";
import axios from "axios";
import cors from "cors";
import NotificationServiceWebsocketClient from "@/api/notification-service/notificationServiceWebsocketClient";

import 'vuetify/styles';
import '@/design/overrides.sass';
import withUUID from "vue-uuid";

configureCompat({
    MODE:3,
    CONFIG_PRODUCTION_TIP: false,
    GLOBAL_PROTOTYPE: true,
    GLOBAL_OBSERVABLE: true
});

export let app = withUUID(createApp(App).use(createPinia())) ;

const requireComponent = require.context(
    '@/components/base', true, /\.vue$/,
)

import upperFirst from 'lodash/upperFirst'
import camelCase from 'lodash/camelCase'

import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import { createPinia } from 'pinia'
import {useUserStore} from "@/stores/userStore";
import {useCatalogStore} from "@/stores/catalogStore";
import {useServicesStore} from "@/stores/servicesStore";
import {useProviderStore} from "@/stores/providerStore";
import {useResourcesStore} from "@/stores/resourcesStore";
import {useNotificationStore} from "@/stores/notificationStore";


requireComponent.keys().forEach(fileName => {
    const componentConfig = requireComponent(fileName)

    const componentName = upperFirst(
        camelCase(fileName.replace(/^\.\//, '').replace(/\.\w+$/, '')),
    )

    app.component(`Base${componentName}`, componentConfig.default || componentConfig)
})

app.component('NoItemAvailableNote', NoItemAvailableNote);


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
        setupTokenInterceptor();
        NotificationServiceWebsocketClient.connect();
        const userStore = useUserStore();
        userStore.getUserDetails().then();

        const catalogStore = useCatalogStore();
        catalogStore.updateCatalogStore().then();

        const serviceStore = useServicesStore();
        serviceStore.initServiceStore().then();
        serviceStore.getServiceInstanceGroups().then();

        const providerStore = useProviderStore();
        providerStore.getVirtualResourceProviders().then();
        providerStore.getServiceHosters().then();

        const resourceStore = useResourcesStore();
        resourceStore.getDeploymentCapabilities().then();
        resourceStore.getResourcesFromBackend().then();
        resourceStore.getResourceAASFromBackend().then();
        resourceStore.getLocations().then();
        resourceStore.getProfiler().then();
        resourceStore.getCluster().then();
        resourceStore.getClusterTypes().then();

        const notificationStore = useNotificationStore();
        notificationStore.getNotifications();
    },
})

Chart.register(...registerables)

// app.prototype.moment = moment

app.use(enums)

const theme = {
    colors: {
        primary: '#004263',
        secondary: '#00A0E3',
        accent: '#17A6A6',
        error: '#FF7A5A',
        warning: '#F39430',
        info: '#71BD86',
        success: '#00A0E3',
    },
    variables:{
        'theme-on-info': '#fff'
    }
};




const v = createVuetify({
    lang: {
        t: (key, ...params) => i18n.t(key, params),
    },
    theme: {
        defaultTheme: 'light',
        themes: {
            dark: theme,
            light: theme,
        },
    },
});

app.use(VueAxios, axios)
app.use(cors)

app.use(router);

import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);
app.use(pinia);


app.use(v);
app.use(i18n);

app.use(require('vue-chartist'));


app.use(VueToast,{
    position: 'bottom',
    duration: 5000,
    dismissible: true,}
);

app.mount('#app');
