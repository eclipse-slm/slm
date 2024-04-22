module.exports = {
  root: true,
  env: {
    node: true,
  },
  extends: [
    'plugin:vue/base',
    'plugin:vuetify/base',
    'plugin:vue/recommended',
    'plugin:vuetify/recommended',
  ],
  rules: {
    // 'no-console': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'no-console': 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    'vue/valid-v-slot': 'warn',
    'vue/no-mutating-props': 'warn',
  },
  parserOptions: {
    parser: '@babel/eslint-parser',
  },
}
