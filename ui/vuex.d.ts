import { Store } from 'vuex'
import {VueKeycloakInstance} from "@dsb-norge/vue-keycloak-js/dist/types";

declare module '@vue/runtime-core' {
    // declare your own store states
    interface State {
        count: number
    }

    // provide typings for `this.$store`
    interface ComponentCustomProperties {
        $store: Store<State>
    }
}

declare module 'vue' {
    import { CompatVue } from '@vue/runtime-dom'
    const Vue: CompatVue
    export default Vue
    export * from '@vue/runtime-dom'
    const { configureCompat } = Vue
    export { configureCompat }
}

declare module '@vue/runtime-core' {
    interface ComponentCustomProperties  {
        $keycloak: VueKeycloakInstance
    }
}