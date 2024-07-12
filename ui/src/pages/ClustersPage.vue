<template>
  <v-container
    id="clusters"
    fluid
    tag="section"
  >
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
      <clusters-overview />
    </div>
  </v-container>
</template>
<script>
import ClustersOverview from "@/components/clusters/ClustersOverview.vue";
import { mapGetters } from 'vuex'
import ApiState from "@/api/apiState";
import {useStore} from "@/stores/store";
import {useResourcesStore} from "@/stores/resourcesStore";

  export default {
      components: {
          ClustersOverview,
      },
    setup(){
        const store = useStore();
        const resourcesStore = useResourcesStore();

        return {store, resourcesStore}
    },
      computed: {
        themeColorMain () {
          return this.store.themeColorMain
        },
        apiStateResources() {
          return this.resourcesStore.apiStateResources
        },
        resources () {
          return this.resourcesStore.resources
        },

          apiStateLoaded () {
              return (this.apiStateResources === ApiState.LOADED || this.apiStateResources === ApiState.UPDATING)
          },
          apiStateLoading () {
              return this.apiStateResources === ApiState.LOADING || this.apiStateResources === ApiState.INIT
          },
          apiStateError () {
              return this.apiStateResources === ApiState.ERROR
          },
      },
  }

</script>