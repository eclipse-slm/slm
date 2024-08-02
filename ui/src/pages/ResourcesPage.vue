<template>
  <v-container
    id="resources"
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
      <resources-overview />
    </div>
  </v-container>
</template>

<script>
import ResourcesOverview from '@/components/resources/ResourcesOverview'
import ApiState from '@/api/apiState'
import {useStore} from "@/stores/store";
import {useResourcesStore} from "@/stores/resourcesStore";

export default {
    components: {
      ResourcesOverview,
    },
    setup(){
      const store = useStore();
      const resourcesStore = useResourcesStore();

      return {store, resourcesStore}
    },
    data () {
      return {
        selectedResource: null,
        createResourceDialog: false,
      }
    },
    computed: {
      themeColorMain() {
        return this.store.themeColorMain
      },
      apiStateResources() {
        return this.resourcesStore.apiStateResources
      },
      resources() {
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
    methods: {
      onResourceSelected (resource) {
        this.selectedResource = resource
      },
    },
  }
</script>

<style scoped />
