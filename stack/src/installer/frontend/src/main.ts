import { createApp } from 'vue';
import App from './App.vue';
import 'vuetify/styles';
import '@mdi/font/css/materialdesignicons.css';
import './design/overrides.sass';
import './styles.css';
import vuetify from './plugins/vuetify';
import { getApiBaseUrl } from './config/runtimeConfig';

console.info('Installer UI API base URL:', getApiBaseUrl() || '/api (same-origin)');
createApp(App).use(vuetify).mount('#app');

