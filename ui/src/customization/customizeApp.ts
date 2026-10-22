import type {App} from 'vue';

/** Hook for consumers: shadow this file to register plugins or components before the app is mounted. */
export default function customizeApp(app: App): void {
    // no-op by default
}
