import Vue from 'vue'
import Vuetify from 'vuetify'
import i18n from '@/localisation/i18n'
import '@/design/overrides.sass'

Vue.use(Vuetify)

// FabOS Theme:
const theme = {
  primary: '#004263',
  secondary: '#00A0E3',
  accent: '#17A6A6',
  error: '#FF7A5A',
  warning: '#F39430',
  info: '#71BD86',
  success: '#00A0E3',
}

// const theme = {
//   primary: '#179C7D',
//   secondary: '#9C27b0',
//   accent: '#9C27b0',
//   info: '#00CAE3',
// }

// const t = createVuetify({
//   lang: {
//     t: (key, ...params) => i18n.t(key, params),
//   },
//   theme: {
//     themes: {
//       dark: theme,
//       light: theme,
//     },
//   },
// });

export default new Vuetify({
  lang: {
    t: (key, ...params) => i18n.t(key, params),
  },
  theme: {
    themes: {
      dark: theme,
      light: theme,
    },
  },
})
