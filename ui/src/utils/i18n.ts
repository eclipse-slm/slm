import { createI18n } from 'vue-i18n';
import {de, en} from "vuetify/locale";
import customizeMessages from "@/customization/customizeMessages";
import enMessages from "@/localisation/en.json";
import deMessages from "@/localisation/de.json";

const messages = customizeMessages({
    en: {
        ...enMessages,
        $vuetify: en,
    },
    de: {
        ...deMessages,
        $vuetify: de,
    },
});

const instance  = createI18n({
    globalInjection: true,
    legacy: false,
    silentTranslationWarn: true,
    messages
});


export default instance;

export const i18n = instance.global;