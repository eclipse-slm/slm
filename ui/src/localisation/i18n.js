import Vue from 'vue'
import VueI18n from 'vue-i18n'
import { createI18n } from 'vue-i18n'

import en from 'vuetify/lib/locale/en'
import de from 'vuetify/lib/locale/de'

Vue.use(VueI18n)

const messages = {
  en: {
    ...require('@/localisation/en.json'),
    $vuetify: en,
  },
  de: {
    ...require('@/localisation/de.json'),
    $vuetify: de,
  },
}

export const i18n = createI18n({
  locale: process.env.VUE_APP_I18N_LOCALE || 'en',
  fallbackLocale: process.env.VUE_APP_I18N_FALLBACK_LOCALE || 'en',
  silentTranslationWarn: true,
  allowComposition: true, // you need to specify that!
  messages
})
