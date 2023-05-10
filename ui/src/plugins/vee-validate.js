import Vue from 'vue'
import {
  extend,
  ValidationObserver,
  ValidationProvider,
  setInteractionMode,
} from 'vee-validate/dist/vee-validate.full.esm'
import {
  email,
  max,
  min,
  numeric,
  double,
  required,
  regex,
} from 'vee-validate/dist/rules'

extend('email', email)
extend('max', max)
extend('min', min)
extend('numeric', numeric)
extend('double', double)
extend('required', required)
extend('regex', regex)

Vue.component('ValidationProvider', ValidationProvider)
Vue.component('ValidationObserver', ValidationObserver)
setInteractionMode('eager')
