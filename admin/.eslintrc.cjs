/* eslint-env node */
const path = require('path')
const fs = require('fs')
let globals = {}
try {
  const p = path.resolve(__dirname, '.eslintrc-auto-import.json')
  if (fs.existsSync(p)) {
    globals = require(p).globals || {}
  }
} catch {
  // .eslintrc-auto-import.json may be missing before first run
}
module.exports = {
  root: true,
  env: {
    browser: true,
    es2021: true,
  },
  globals,
  extends: [
    'eslint:recommended',
    'plugin:vue/vue3-recommended',
  ],
  parserOptions: {
    ecmaVersion: 'latest',
    sourceType: 'module',
    extraFileExtensions: ['.vue'],
  },
  plugins: ['vue'],
  settings: {
    'import/resolver': {
      typescript: {},
    },
  },
  ignorePatterns: [
    'node_modules/',
    'dist/',
    'build/',
    'public/',
    '*.min.js',
    'auto-imports.d.ts',
    'components.d.ts',
    'typed-router.d.ts',
  ],
  rules: {
    'vue/multi-word-component-names': 'off',
    'vue/no-v-html': 'off',
    // Чтобы CI не падал на шаблоне (Vuetify, демо и т.д.) — спорные правила в warn
    'no-unused-vars': ['warn', { argsIgnorePattern: '^_', varsIgnorePattern: '^_' }],
    'vue/require-default-prop': 'warn',
    'vue/valid-v-slot': 'warn',
    'vue/no-unused-vars': 'warn',
    'no-inner-declarations': 'warn',
    'no-unsafe-optional-chaining': 'warn',
    'no-useless-escape': 'warn',
    'no-undef': 'warn',
  },
  overrides: [
    {
      files: ['vite.config.js', 'vite.config.ts', '**/build-icons.js', '**/handlers/**/*.js'],
      env: { node: true },
      globals: { __dirname: 'readonly', __filename: 'readonly', process: 'readonly', module: 'readonly', require: 'readonly' },
    },
  ],
}
