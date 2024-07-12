<template>
  <v-card>
    <v-container>
      <v-row class="my-3">
        <v-col class="text-center">
          <v-btn
            color="secondary"
            size="x-large"
            tile
            @click="$emit('page-changed', ResourcesCreateDialogPage.ADD_EXISTING_RESOURCE_HOST)"
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
            @click="$emit('page-changed', ResourcesCreateDialogPage.ADD_EXISTING_RESOURCE_CLUSTER)"
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
  import { mapGetters } from 'vuex'
  import ResourcesCreateDialogPage from '@/components/resources/dialogs/create/ResourcesCreateDialogPage'
  import {useResourcesStore} from "@/stores/resourcesStore";

  export default {
    name: 'ResourcesCreateDialogPageAddExistingResource',
    enums: {
      ResourcesCreateDialogPage,
    },
    setup(){
      const resourceStore = useResourcesStore();
      return {resourceStore};
    },
    computed: {
      ResourcesCreateDialogPage() {
        return ResourcesCreateDialogPage
      },
      availableClusterTypes() {
        return this.resourceStore.availableClusterTypes
      },
    },
    mounted() {
      this.$emit('title-changed', 'Add existing resource')
    },
  }
</script>
