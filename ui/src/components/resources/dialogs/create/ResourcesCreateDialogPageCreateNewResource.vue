<template>
  <v-card>
    <v-container>
      <v-row class="my-3">
        <v-col class="text-center">
          <v-btn
            color="secondary"
            size="x-large"
            tile
            :disabled="virtualResourceProviders.length === 0"
            @click="$emit('page-changed', ResourcesCreateDialogPage.CREATE_RESOURCE_HOST)"
          >
            <v-icon
              class="mr-5 ml-2"
              size="x-large"
              start
            >
              mdi-desktop-classic
            </v-icon>
            Host
          </v-btn>
        </v-col>
        <v-col class="text-center">
          <v-btn
            id="resource-create-button-cluster"
            color="secondary"
            size="x-large"
            tile
            :disabled="availableClusterTypes.length === 0"
            @click="$emit('page-changed', ResourcesCreateDialogPage.CREATE_RESOURCE_CLUSTER)"
          >
            <v-icon
              class="mr-5 ml-2"
              size="x-large"
              start
            >
              mdi-server
            </v-icon>
            Cluster
          </v-btn>
        </v-col>
      </v-row>
    </v-container>

    <v-card-actions>
      <v-btn
        variant="text"
        @click="$emit('page-changed', ResourcesCreateDialogPage.START)"
      >
        Back
      </v-btn>
      <v-spacer />
      <v-btn
        variant="text"
        @click="$emit('canceled')"
      >
        Cancel
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>

import ResourcesCreateDialogPage from '@/components/resources/dialogs/create/ResourcesCreateDialogPage'
import {useResourceClustersStore} from "@/stores/resourceClustersStore";
import {useProviderStore} from "@/stores/providerStore";

export default {
    name: 'ResourcesCreateDialogPageCreateNewResource',
    enums: {
      ResourcesCreateDialogPage,
    },
    setup(){
      const providerStore = useProviderStore();
      const resourceClustersStore = useResourceClustersStore();

      return {providerStore, resourceClustersStore};
    },
    computed: {
      virtualResourceProviders() {
        return this.providerStore.virtualResourceProviders
      },
      availableClusterTypes() {
        return this.resourceClustersStore.availableClusterTypes
      },
    },
    mounted() {
      this.$emit('title-changed', 'Create new resource')
    },
  }
</script>
