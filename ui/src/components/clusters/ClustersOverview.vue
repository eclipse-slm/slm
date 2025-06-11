<template>
  <div>
    <base-material-card>
      <template #heading>
        <overview-heading text="Clusters" />
      </template>

      <no-item-available-note
        v-if="!resourceClustersStore.clusters.length"
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
      icon="mdi-plus"
      class="mx-4"
      elevation="15"
      color="primary"
      location="right bottom"
      :app="true"
      @click="showCreateDialog = true"
    />

    <clusters-create-dialog
      :show="showCreateDialog"
      @canceled="showCreateDialog = false"
    />
  </div>
</template>

<script>

import OverviewHeading from "@/components/base/OverviewHeading.vue";
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import ClustersCreateDialog from "@/components/clusters/dialogs/ClustersCreateDialog.vue";
import ResourcesTableClusters from "@/components/resources/ResourcesTableClusters.vue";
import {useResourceClustersStore} from "@/stores/resourceClustersStore";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";

export default {
  name: 'ClustersOverview',
  components: {
    ResourcesTableClusters,
    OverviewHeading,
    NoItemAvailableNote,
    ClustersCreateDialog
  },
  setup(){
    const resourceClustersStore = useResourceClustersStore();
    const resourceDevicesStore = useResourceDevicesStore();
    return {resourceClustersStore, resourceDevicesStore}
  },
  data () {
    return {
      showCreateDialog: false,
      showDeleteDialog: false,
    }
  },
  computed: {
  },
  mounted () {
    this.resourceDevicesStore.getDeploymentCapabilities();
  },
  methods: {
  },
}
</script>
