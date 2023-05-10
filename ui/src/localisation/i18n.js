import Vue from 'vue'
import VueI18n from 'vue-i18n'

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

export default new VueI18n({
  locale: process.env.VUE_APP_I18N_LOCALE || 'en',
  fallbackLocale: process.env.VUE_APP_I18N_FALLBACK_LOCALE || 'en',
  silentTranslationWarn: true,
  messages,
})
