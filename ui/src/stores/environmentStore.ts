import { defineStore } from 'pinia';
import { computed, ref } from 'vue';

const isProduction = import.meta.env.MODE === 'production';

export const useEnvStore = defineStore('envStore', () => {
    // States
    const _appVersion = ref(import.meta.env.VITE_APP_VERSION || (isProduction ? '/__ENV_APP_VERSION__PLACEHOLDER__/' : ''));

    const _i18nLocale = ref(import.meta.env.VITE_APP_I18N_LOCALE || (isProduction ? '/__ENV_I18N_LOCALE__PLACEHOLDER__/' : ''));
    const _i18nLocaleFallback = ref(import.meta.env.VITE_APP_I18N_LOCALE_FALLBACK || (isProduction ? '/__ENV_I18N_LOCALE_FALLBACK__PLACEHOLDER__/' : ''));

    const _notificationServiceUrl = ref(import.meta.env.VITE_APP_NOTIFICATION_SERVICE_URL || (isProduction ? '/__ENV_NOTIFICATION_SERVICE_URL__PLACEHOLDER__/' : ''));
    const _resourceManagementUrl = ref(import.meta.env.VITE_APP_RESOURCE_MANAGEMENT_URL || (isProduction ? '/__ENV_RESOURCE_MANAGEMENT_URL__PLACEHOLDER__/' : ''));
    const _serviceManagementUrl = ref(import.meta.env.VITE_APP_SERVICE_MANAGEMENT_URL || (isProduction ? '/__ENV_SERVICE_MANAGEMENT_URL__PLACEHOLDER__/' : ''));
    const _catalogServiceUrl = ref(import.meta.env.VITE_APP_CATALOG_SERVICE_URL || (isProduction ? '/__ENV_CATALOG_SERVICE_URL__PLACEHOLDER__/' : ''));

    const _keycloakUrl = ref(import.meta.env.VITE_APP_KEYCLOAK_URL || (isProduction ? '/__ENV_KEYCLOAK_URL__PLACEHOLDER__/' : ''));
    const _keycloakRealm = ref(import.meta.env.VITE_APP_KEYCLOAK_REALM || (isProduction ? '/__ENV_KEYCLOAK_REALM__PLACEHOLDER__/' : ''));
    const _keycloakClientId = ref(import.meta.env.VITE_APP_KEYCLOAK_CLIENT_ID || (isProduction ? '/__ENV_KEYCLOAK_CLIENT_ID__PLACEHOLDER__/' : ''));

    const _awxUrl = ref(import.meta.env.VITE_APP_AWX_URL || (isProduction ? '/__ENV_AWX_URL__PLACEHOLDER__/' : ''));

    const _basyxAasGuiUrl = ref(import.meta.env.VITE_APP_BASYX_AAS_GUI_URL || (isProduction ? '/__ENV_BASYX_AAS_GUI_URL__PLACEHOLDER__/' : ''));

    // Getters
    const appVersion = computed(() => _appVersion.value);

    const i18nLocale = computed(() => _i18nLocale.value);
    const i18nLocaleFallback = computed(() => _i18nLocaleFallback.value);

    const notificationServiceUrl = computed(() => _notificationServiceUrl.value);
    const resourceManagementUrl = computed(() => _resourceManagementUrl.value);
    const serviceManagementUrl = computed(() => _serviceManagementUrl.value);
    const catalogServiceUrl = computed(() => _catalogServiceUrl.value);

    const keycloakUrl = computed(() => _keycloakUrl.value);
    const keycloakRealm = computed(() => _keycloakRealm.value);
    const keycloakClientId = computed(() => _keycloakClientId.value);

    const awxUrl = computed(() => _awxUrl.value);

    const basyxAasGuiUrl = computed(() => _basyxAasGuiUrl.value);

    return {
        appVersion,
        i18nLocale,
        i18nLocaleFallback,
        notificationServiceUrl,
        resourceManagementUrl,
        serviceManagementUrl,
        catalogServiceUrl,
        keycloakUrl,
        keycloakRealm,
        keycloakClientId,
        awxUrl,
        basyxAasGuiUrl,
    };
});