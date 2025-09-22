import {createApp} from 'vue';
import App from './App.vue'

import router from './pages/router';
import 'chartist/dist/chartist.min.css';
import 'material-design-icons-iconfont/dist/material-design-icons.css';
import setupTokenInterceptor from '@/utils/tokenInterceptor';
import VueKeycloakJs from '@dsb-norge/vue-keycloak-js'
import VueToast from 'vue-toast-notification';
import 'vue-toast-notification/dist/theme-sugar.css';
import enums from 'vue-enums';
import moment from 'moment';

import {Chart, registerables} from 'chart.js';
import {createVuetify} from "vuetify";
import cors from "cors";
import NotificationServiceWebsocketClient from "@/api/notification-service/notificationServiceWebsocketClient";

import 'vuetify/styles';
import '@/design/overrides.sass';
import upperFirst from 'lodash/upperFirst';
import camelCase from 'lodash/camelCase';

import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import {createPinia} from 'pinia';
import {useUserStore} from "@/stores/userStore";
import {useCatalogStore} from "@/stores/catalogStore";
import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";
import {useServiceInstancesStore} from "@/stores/serviceInstancesStore";
import {useProviderStore} from "@/stores/providerStore";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import {useResourceClustersStore} from "@/stores/resourceClustersStore";
import {useDiscoveryStore} from "@/stores/discoveryStore";
import {useNotificationStore} from "@/stores/notificationStore";
import {useJobsStore} from "@/stores/jobsStore";
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate';
import * as yup from "yup";
import i18nInstance from '@/utils/i18n'
import {de, en} from "vuetify/locale";
import yupValidateIPv4 from "@/utils/yup.custom";
import {VueKeycloakInstance} from "@dsb-norge/vue-keycloak-js/dist/types";
import {useEnvStore} from "@/stores/environmentStore";
import updateToken from "@/utils/updateToken";
import {useCapabilitiesStore} from "@/stores/capabilitiesStore";

console.log("App Mode: " + import.meta.env.MODE)

const app = createApp(App);

const pinia = createPinia();
pinia.use(piniaPluginPersistedstate);
app.use(pinia);

const envStore = useEnvStore();

const components = import.meta.glob('@/components/base/*.vue');
for (const path in components) {
    components[path]().then((module) => {
        const componentName = upperFirst(
            camelCase(path.replace(/^.*[\\\/]/, '').replace(/\.\w+$/, ''))
        );
        app.component(`Base${componentName}`, module.default);
    });
}

app.component('NoItemAvailableNote', NoItemAvailableNote);

app.use(VueKeycloakJs, {
    init: {
        onLoad: 'login-required',
        checkLoginIframe: false,
    },
    config: {
        url: envStore.keycloakUrl,
        realm: envStore.keycloakRealm,
        clientId: envStore.keycloakClientId,
    },
    onInitError(error, keycloakError) {
        console.error('Keycloak init error', error, keycloakError);
    },
    onReady (keycloak) {
        updateToken();
        setupTokenInterceptor();

        // Connect to notification service via WebSocket
        NotificationServiceWebsocketClient.connect();

        // Update stores
        const userStore = useUserStore();
        userStore.updateStore();
        const catalogStore = useCatalogStore();
        catalogStore.updateStore();
        const serviceOfferingsStore = useServiceOfferingsStore();
        serviceOfferingsStore.updateStore();
        const serviceInstancesStore = useServiceInstancesStore();
        serviceInstancesStore.updateStore();
        const providerStore = useProviderStore();
        providerStore.updateStore();
        const resourceDevicesStore = useResourceDevicesStore();
        resourceDevicesStore.updateStore();
        const resourceClustersStore = useResourceClustersStore();
        resourceClustersStore.updateStore();
        const capabilitiesStore = useCapabilitiesStore();
        capabilitiesStore.updateStore();
        const discoveryStore = useDiscoveryStore();
        discoveryStore.updateStore();
        const notificationStore = useNotificationStore();
        notificationStore.updateStore();
        const jobsStore = useJobsStore();
        jobsStore.updateStore();
    },
});

//  Allow usage of this.$keycloak in components
declare module '@vue/runtime-core' {
    interface ComponentCustomProperties  {
        $keycloak: VueKeycloakInstance
    }
}

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

const vuetify = createVuetify({
    // lang: {
    //     t: (key, ...params) => i18n.t(key, params),
    // },
    theme: {
        defaultTheme: 'light',
        themes: {
            dark: theme,
            light: theme,
        },
    },
});
app.use(vuetify);

app.use(cors)

app.use(router);


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


app.use(i18nInstance);

app.use(VueToast,{
    position: 'bottom-right',
    duration: 5000,
    dismissible: true,}
);

app.mount('#app');

yup.addMethod(yup.string, 'ipv4', yupValidateIPv4);

const globals = app.config.globalProperties
export { globals }
