import {createApp} from 'vue'
import App from './App.vue'
import router from './pages/router'
import 'chartist/dist/chartist.min.css'
import 'material-design-icons-iconfont/dist/material-design-icons.css'
import setupTokenInterceptor from '@/utils/tokenInterceptor'
import VueKeycloakJs from '@dsb-norge/vue-keycloak-js'
import getEnv from '@/utils/env'
import VueToast from 'vue-toast-notification'
import 'vue-toast-notification/dist/theme-sugar.css'
import enums from 'vue-enums'
import moment from 'moment'

import {Chart, registerables} from 'chart.js'
import {createVuetify} from "vuetify";
import VueAxios from "vue-axios";
import axios from "axios";
import cors from "cors";
import NotificationServiceWebsocketClient from "@/api/notification-service/notificationServiceWebsocketClient";

import 'vuetify/styles';
import '@/design/overrides.sass';
import withUUID from "vue-uuid";
import upperFirst from 'lodash/upperFirst'
import camelCase from 'lodash/camelCase'

import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import {createPinia} from 'pinia'
import {useUserStore} from "@/stores/userStore";
import {useCatalogStore} from "@/stores/catalogStore";
import {useServicesStore} from "@/stores/servicesStore";
import {useProviderStore} from "@/stores/providerStore";
import {useResourcesStore} from "@/stores/resourcesStore";
import {useNotificationStore} from "@/stores/notificationStore";
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
import * as yup from "yup";
import {createI18n} from "vue-i18n";
import {de, en} from "vuetify/locale";

export let app = withUUID(createApp(App)) ;

const requireComponent = require.context(
    '@/components/base', true, /\.vue$/,
)


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

Chart.register(...registerables);

app.config.globalProperties.moment = moment;

app.use(enums);

const theme = {
    colors: {
        primary: '#004263',
        secondary: '#00A0E3',
        accent: '#17A6A6',
        error: '#FF7A5A',
        warning: '#F39430',
        info: '#71BD86',
        success: '#00A0E3',
        disable: '#BBBBBBD1'
    },
    variables:{
        'theme-on-info': '#fff',
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

const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);
app.use(pinia);

app.use(v);

const messages = {
    en: {
        ...require('@/localisation/en.json'),
        $vuetify: en,
    },
    de: {
        ...require('@/localisation/de.json'),
        $vuetify: de,
    },
};

const i18n = createI18n({
    globalInjection: true,
    legacy: false,
    locale: process.env.VUE_APP_I18N_LOCALE || 'en',
    fallbackLocale: process.env.VUE_APP_I18N_FALLBACK_LOCALE || 'en',
    silentTranslationWarn: true,
    messages
});

app.use(i18n);

app.use(require('vue-chartist'));


app.use(VueToast,{
    position: 'bottom',
    duration: 5000,
    dismissible: true,}
);

app.mount('#app');


function ipv4(message = 'Invalid IP address') {
    return this.matches(/(^(\d{1,3}\.){3}(\d{1,3})$)/, {
        message,
        excludeEmptyString: true
    }).test('ip', message, value => {
        return value === undefined || value.trim() === '' ? true
            : value.split('.').find(i => parseInt(i, 10) > 255) === undefined;
    });
}

yup.addMethod(yup.string, 'ipv4', ipv4);
