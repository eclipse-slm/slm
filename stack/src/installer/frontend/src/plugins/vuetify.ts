import { createVuetify } from 'vuetify';
import { aliases, mdi } from 'vuetify/iconsets/mdi';

const slmTheme = {
  dark: false,
  colors: {
    primary: '#004263',
    secondary: '#00A0E3',
    accent: '#17A6A6',
    error: '#FF7A5A',
    warning: '#F39430',
    info: '#71BD86',
    success: '#00A0E3',
    disable: '#BBBBBBD1',
    background: '#eeeeee',
    surface: '#ffffff'
  },
  variables: {
    'theme-on-info': '#ffffff'
  }
};

export default createVuetify({
  theme: {
    defaultTheme: 'slmLight',
    themes: {
      slmLight: slmTheme,
      slmDark: slmTheme
    }
  },
  defaults: {
    VCard: {
      rounded: 'lg',
      elevation: 2
    },
    VBtn: {
      rounded: 'pill',
      elevation: 0
    },
    VTextField: {
      color: 'primary',
      variant: 'outlined',
      density: 'comfortable'
    },
    VAlert: {
      density: 'comfortable',
      variant: 'tonal'
    },
    VChip: {
      label: true,
      size: 'small'
    }
  },
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: {
      mdi
    }
  }
});

