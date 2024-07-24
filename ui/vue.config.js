const path = require('path');
const webpack = require('webpack');
module.exports = {
  chainWebpack: (config) => {
    config.module
        .rule('vue')
        .use('vue-loader');
  },
  productionSourceMap: true, // NOTE: this is default
  configureWebpack: {
    devtool: 'source-map',
    context: __dirname,
    resolve: {
      extensions: ['.ts', '.js', '.vue'],
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },
    plugins: [
      new webpack.DefinePlugin({
        __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: 'false',
      })
    ],
    module: {
      rules: [
        // {
        //   test: /\.vue$/,
        //   loader: 'vue-loader',
        // },
        {
          test: /\.ts$/,
          loader: 'ts-loader',
          options: {
            appendTsSuffixTo: [/\.vue$/],
            // transpileOnly: true,
          },
        },
      ],
    },
  },
  devServer: {
    allowedHosts: ['all'],
    // https://gist.github.com/brenopolanski/7f084f2ab8f817f6160deae1be629520
    proxy: {
      '/notification-service/*': {
        target: `${process.env.VUE_APP_NOTIFICATION_SERVICE_URL}`,
        pathRewrite: {
          '^/notification-service': '',
        },
        changeOrigin: false,
        secure: false,
      },
      '/resource-management/*': {
        target: `${process.env.VUE_APP_RESOURCE_REGISTRY_URL}`,
        pathRewrite: {
          '^/resource-management': '',
        },
        changeOrigin: false,
        secure: false,
      },
      '/service-management/*': {
        target: `${process.env.VUE_APP_SERVICE_REGISTRY_URL}`,
        pathRewrite: {
          '^/service-management': '',
        },
        changeOrigin: false,
        secure: false,
      },
      '/slm-catalog/*': {
        target: `${process.env.VUE_APP_CATALOG_SERVICE_URL}`,
        pathRewrite: {
          '^/slm-catalog': '',
        },
        changeOrigin: false,
        secure: false,
      },
    },
  },

  transpileDependencies: ['vuetify'],

  pluginOptions: {
    i18n: {
      locale: 'en',
      fallbackLocale: 'en',
      localeDir: 'locales',
      enableInSFC: false,
    },
  },
}
