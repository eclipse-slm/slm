import axios from 'axios'
import {defineStore} from "pinia";

interface StoreState{
  barColor: string,
  themeColorMain_: string,
  drawer: null | any,
  allInstances: any[],
  Header: any,
  applicationUrl: string
}

export const useStore = defineStore('store', {
  state:():StoreState => ({
    barColor: 'rgba(70, 70, 70, 0.8), rgba(0, 0, 0, .8)',
    themeColorMain_: '#179C7D',
    drawer: null,
    allInstances: [],
    Header: null,
    applicationUrl: ''
  }),
  getters: {
    themeColorMain: (state) => {
      return state.themeColorMain_
    },
  },
  actions: {
    async loadInstances () {
      this.allInstances = [];
      const allInstances = await axios.get('http://localhost:9090/devices/service-instances',
          { headers: this.Header })
      for (let i = 0; i < allInstances.data.length; i++) {
        console.log(allInstances.data[i])
        const id = allInstances.data[i]
        const instanceURL = `${this.applicationUrl}instance/${id}`
        console.log('instanceURL')
        console.log(instanceURL)
        await this.getInstance(instanceURL)
      }
    },

    // All sites
    async getInstance (instanceURL) {
      const res = await axios.get(instanceURL)
      console.log(instanceURL)

      const appName = res.data[0].app

      const onlineStatus = await this.onlineStatus(instanceURL)
      // context.commit('SET_ONLINE_STATUS', { appName, onlineStatus })
    },
    async onlineStatus (url) {
      let onlineDevice = false
      await axios({
        method: 'get',
        url: url,
        headers: this.Header,
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
