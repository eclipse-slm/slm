import { createApp } from 'vue';
import App from '@/App.vue';

// The app lives in its own module so that other modules can reach it without importing the entry (main.ts):
// importing the entry from a module makes Vite (dev, package under node_modules) load a second copy of it.
export const app = createApp(App);

export const globals = app.config.globalProperties;
