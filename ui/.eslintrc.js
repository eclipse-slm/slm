module.exports = {
  root: true,
  env: {
    es2021: true,
  },
  extends: [
    'plugin:vue/base',
    'plugin:vuetify/base',
    'plugin:vue/recommended',
    'plugin:vuetify/recommended',
    'plugin:vue/vue3-recommended',
    '@vue/typescript/recommended'
  ],
  rules: {
    'no-console': 'off',
    'vue/valid-v-slot': 'warn',
    'vue/no-mutating-props': 'warn',
  },
  parserOptions: {
  },
};
