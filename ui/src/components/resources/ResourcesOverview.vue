<template>
  <div>
    <base-material-card>
      <template #heading>
        <overview-heading text="Devices" />
      </template>

      <no-item-available-note
        v-if="!resources.length && !clusters.length"
        item="Resource and Cluster"
      />

      <v-card-text v-else>
        <v-row>
          <resources-table-single-hosts
            v-if="resources.length > 0"
            class="mt-0 flex"
            @resource-selected="onResourceSelected"
          />
        </v-row>

        <v-row
          v-if="clusters.length > 0"
          class="mt-10"
        >
          <resources-table-clusters
            class="mt-0 flex"
          />
        </v-row>
      </v-card-text>
    </base-material-card>

    <resources-info-dialog
      :resource="selectedResource"
      @closed="selectedResource = null"
    />
    <v-fab
      :active="!showCreateButton"
      class="mb-10"
      elevation="15"
      color="primary"
      icon="mdi-plus"
      location="top end"
      absolute
      offset
      @click="showCreateDialog = true"
    />

    <resources-create-dialog
      :show="showCreateDialog"
      @canceled="showCreateDialog = false"
    />
  </div>
</template>

<script>
import ResourcesInfoDialog from '@/components/resources/dialogs/ResourcesInfoDialog'
import ResourcesCreateDialog from '@/components/resources/dialogs/create/ResourcesCreateDialog'
import ResourcesTableSingleHosts from '@/components/resources/ResourcesTableSingleHosts'
import ResourcesTableClusters from '@/components/resources/ResourcesTableClusters'
import OverviewHeading from "@/components/base/OverviewHeading.vue";
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import {useResourcesStore} from "@/stores/resourcesStore";

export default {
    name: 'ResourcesOverview',
    components: {
      OverviewHeading,
      ResourcesInfoDialog,
      ResourcesCreateDialog,
      ResourcesTableSingleHosts,
      ResourcesTableClusters,
      NoItemAvailableNote,
    },
    setup(){
      const resourceStore = useResourcesStore();
      return {resourceStore};
    },
    data () {
      return {
        selectedResource: null,
        showCreateDialog: false,
        showDeleteDialog: false,
        showCreateButton: false,
      }
    },
    computed: {
      resources () {
        return this.resourceStore.resources
      },
      clusters () {
        return this.resourceStore.clusters
      },
    },
    mounted () {
      this.resourceStore.getDeploymentCapabilities();
    },
    methods: {
      getResourcesFromBackend: () => {
        return this.resourceStore.getResourcesFromBackend()
      },

      onResourceSelected (resource) {
        this.selectedResource = resource
      },
    },
  }
</script>
