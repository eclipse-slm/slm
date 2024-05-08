import Vue from 'vue'
import Vuex, {createStore} from 'vuex'
import axios from 'axios'
import VuexPersistence from 'vuex-persist'
import resourcesStore from '@/store/resourcesStore'
import servicesStore from '@/store/servicesStore'
import userStore from '@/store/userStore'
import jobsStore from '@/store/jobsStore'
import notificationStore from '@/store/notificationStore'
import overviewStore from '@/store/overviewStore'
import providerStore from '@/store/providerStore'
import catalogStore from "@/store/catalogStore";


const vuexLocal = new VuexPersistence({
  storage: window.sessionStorage,
})

export const store = createStore({
  modules: {
    jobsStore,
    resourcesStore,
    providerStore,
    servicesStore,
    userStore,
    notificationStore,
    overviewStore,
    catalogStore,
  },
  plugins: [vuexLocal.plugin],
  state: {
    barColor: 'rgba(70, 70, 70, 0.8), rgba(0, 0, 0, .8)',
    themeColorMain: '#179C7D',
    drawer: null,
  },
  getters: {
    themeColorMain: (state) => {
      return state.themeColorMain
    },
  },
  mutations: {
    SET_DRAWER (state, payload) {
      state.drawer = payload
    },
    SET_THEME_COLOR_MAIN (state, color) {
      state.themeColorMain = color
    },
  },
  actions: {
    async loadInstances (context) {
      context.state.allInstances = []
      const allInstances = await axios.get('http://localhost:9090/devices/service-instances', { tableHeaders: context.state.Header })
      for (let i = 0; i < allInstances.data.length; i++) {
        console.log(allInstances.data[i])
        const id = allInstances.data[i]
        const instanceURL = `${context.state.applicationUrl}instance/${id}`
        console.log('instanceURL')
        console.log(instanceURL)
        await context.dispatch('getInstance', instanceURL)
      }
    },

    // All sites
    async getInstance (context, instanceURL) {
      const res = await axios.get(instanceURL)
      console.log(instanceURL)

      const appName = res.data[0].app

      const onlineStatus = await context.dispatch('onlineStatus', instanceURL)
      context.commit('SET_ONLINE_STATUS', { appName, onlineStatus })
    },
    async onlineStatus (commit, url) {
      let onlineDevice = false
      await axios({
        method: 'get',
        url: url,
        headers: commit.state.Header,
      }).then(function (response) {
        if (response.status === 200) {
          onlineDevice = true
        }
      }).catch(error => {
        console.log(error)
      })
      return onlineDevice
    },
  },
});

