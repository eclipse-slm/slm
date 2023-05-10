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

  export default {
      components: {
          ClustersOverview,
      },
      computed: {
          ...mapGetters([
              'themeColorMain',
              'apiStateResources',
              'resources',
          ]),
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