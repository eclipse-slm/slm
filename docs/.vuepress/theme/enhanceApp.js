/**
 * Client app enhancement file.
 *
 * https://v1.vuepress.vuejs.org/guide/basic-config.html#app-level-enhancements
 */

 import Vuetify from "vuetify";
 import "vuetify/dist/vuetify.min.css";

export default ({
  Vue,      // The version of Vue being used in the VuePress app
  options,  // The options for the root Vue instance
  router,   // The router instance for the app
  siteData, // Site metadata
}) => {
  Vue.use(Vuetify);
  options.vuetify = new Vuetify({})
};