<template>
  <div>
    <base-material-card
      class="px-5 py-3"
    >
      <template #heading>
        <overview-heading text="Clusters" />
      </template>

      <no-item-available-note
        v-if="!clusters.length"
        item="Cluster"
      />

      <v-card-text v-else>
        <resources-table-clusters
          class="mt-0 flex"
        />
      </v-card-text>
    </base-material-card>


    <v-fab
      id="resources-button-add-resource"
      class="mb-10"
      elevation="15"
      color="primary"
      icon="mdi-plus"
      location="top end"
      absolute
      offset
      @click="showCreateDialog = true"
    />

    <clusters-create-dialog
      :show="showCreateDialog"
      @canceled="showCreateDialog = false"
    />
  </div>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'
import OverviewHeading from "@/components/base/OverviewHeading.vue";
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import ClustersCreateDialog from "@/components/clusters/dialogs/ClustersCreateDialog.vue";
import ResourcesTableClusters from "@/components/resources/ResourcesTableClusters.vue";

export default {
  name: 'ClustersOverview',
  components: {
    ResourcesTableClusters,
    OverviewHeading,
    NoItemAvailableNote,
    ClustersCreateDialog
  },
  data () {
    return {
      showCreateDialog: false,
      showDeleteDialog: false,
    }
  },
  computed: {
    ...mapGetters([
      'clusters',
    ])
  },
  mounted () {
    this.$store.dispatch('getDeploymentCapabilities')
  },
  methods: {
    ...mapActions([
      'getResourcesFromBackend',
    ])
  },
}
</script>
