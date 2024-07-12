<template>
  <v-container fluid>
    <div
      v-if="apiStateLoading"
      class="text-center"
    >
      <v-progress-circular
        :size="70"
        :width="7"
        color="primary"
        indeterminate
      />
    </div>
    <div v-if="apiStateError">
      Error
    </div>

    <div v-if="apiStateLoaded">
      <service-instances-overview />
    </div>
  </v-container>
</template>

<script>
  import { mapGetters } from 'vuex'
  import ApiState from '@/api/apiState.js'
  import ServiceInstancesOverview from '@/components/services/ServiceInstancesOverview'
  import {useServicesStore} from "@/stores/servicesStore";
  import {useStore} from "@/stores/store";
  import {useResourcesStore} from "@/stores/resourcesStore";

  export default {
    components: {
      ServiceInstancesOverview,
    },
    setup(){
      const servicesStore = useServicesStore();

      return {servicesStore}
    },
    data () {
      return {
      }
    },
    created () {
      this.servicesStore.getServices();
      this.servicesStore.getServiceOfferings();
    },
    computed: {
      apiStateServices() {
        return this.servicesStore.apiStateServices
      },
      apiStateLoaded () {
        return this.apiStateServices.services === ApiState.LOADED
      },
      apiStateLoading () {
        if (this.apiStateServices.services === ApiState.INIT) {
          this.servicesStore.getServices();
        }
        return this.apiStateServices.services === ApiState.LOADING || this.apiStateServices.services === ApiState.INIT
      },
      apiStateError () {
        return this.apiStateServices.services === ApiState.ERROR
      },
    },
  }

</script>
