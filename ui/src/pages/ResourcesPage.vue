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
  import { mapGetters } from 'vuex'
  import ResourcesOverview from '@/components/resources/ResourcesOverview'
  import ApiState from '@/api/apiState'

  export default {
    components: {
      ResourcesOverview,
    },
    data () {
      return {
        selectedResource: null,
        createResourceDialog: false,
      }
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
    methods: {
      onResourceSelected (resource) {
        this.selectedResource = resource
      },
    },
  }
</script>

<style scoped
/>
