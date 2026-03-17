import type { Theme } from 'vitepress';
import DefaultTheme from 'vitepress/theme';
import { enhanceAppWithTabs } from 'vitepress-plugin-tabs/client';
import versions from '../components/versions.vue';
import Layout from "./Layout.vue";

export default <Theme>{
    extends: DefaultTheme,
    Layout,
    enhanceApp({ app }) {
        enhanceAppWithTabs(app);
        app.component('versions', versions);
        // https://vuejs.org/api/application.html#app-config-globalproperties
        app.config.globalProperties.$slm = {
            version: {
                full: '22.7.0'
            },
            ports: {
                catalogService: '10000',
                platformManagement: '9001',
                notificationService: '9002',
                informationService: '9003',
                resourceManagement: '9010',
                serviceManagement: '9020'
            },
            basePaths: {
                catalogService: '/catalog-service',
                platformManagement: '/platform-management',
                notificationService: '/notification-service',
                informationService: '/information-service',
                resourceManagement: '/resource-management',
                serviceManagement: '/service-management'
            }
        };
        app.config.globalProperties.$awx = {
            version: {
                full: '22.7.0'
            },
            ports: {
                web: '8013'
            }
        };
        app.config.globalProperties.$consul = {
            version: {
                full: '1.22.3'
            },
            ports: {
                http: '8500'
            }
        };
        app.config.globalProperties.$keycloak = {
            version: {
                full: '26.4.0'
            },
            ports: {
                https: '443'
            }
        };
        app.config.globalProperties.$minio = {
            version: {
                full: 'RELEASE.2023-12-20T01-00-02Z'
            },
            ports: {
                console: '9091',
                api: '9000'
            }
        };
        app.config.globalProperties.$prometheus = {
            version: {
                full: 'v2.43.0'
            },
            ports: {
                http: '9090',
            }
        };
        app.config.globalProperties.$rabbitmq = {
            version: {
                full: '4.0.5-management-alpine'
            },
            ports: {
                http: '15672',
            }
        };
        app.config.globalProperties.$traefik = {
            version: {
                full: 'v3.3.4'
            },
            ports: {
                dashboard: '7071'
            }
        };
        app.config.globalProperties.$vault = {
            version: {
                full: '1.21.2'
            },
            ports: {
                http: '8200'
            }
        };
        app.config.globalProperties.$basyx = {
            version: {
                discovery: '2.0.0-milestone-07',
                shell_registry: '2.0.0-milestone-07',
                sm_registry: '2.0.0-milestone-07',
                env: '2.0.0-milestone-07',
                gui: 'v2-251001',
            },
            ports: {
                discovery: '8084',
                shell_registry: '8082',
                sm_registry: '8083',
                env: '8081',
                gui: '80',
            },
            basePaths: {
                discovery: '/aas/discovery',
                shell_registry: '/aas/shell-registry',
                sm_registry: '/aas/submodel-registry',
                env: '/aas/environment',
                gui: '/aas/ui',
            }
        };
    },
};
