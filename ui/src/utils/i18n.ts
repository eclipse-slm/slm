import { createI18n } from 'vue-i18n';
import {de, en} from "vuetify/locale";

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

const instance  = createI18n({
    globalInjection: true,
    legacy: false,
    silentTranslationWarn: true,
    messages
});


export default instance;

export const i18n = instance.global;