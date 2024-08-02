<template>
  <v-card>
    <v-container>
      <v-row class="my-3">
        <v-col class="text-center">
          <v-btn
            id="resource-create-button-add-existing"
            color="secondary"
            size="x-large"
            tile
            @click="$emit('page-changed', ClusterCreateDialogPage.ADD_EXISTING_CLUSTER)"
          >
            <v-icon
              class="mr-5"
              size="x-large"
              start
            >
              mdi-plus
            </v-icon>
            Add existing Cluster
          </v-btn>
        </v-col>
        <v-col class="text-center">
          <v-btn
            id="resource-create-button-create-new"
            color="secondary"
            size="x-large"
            tile
            @click="$emit('page-changed', ClusterCreateDialogPage.CREATE_CLUSTER)"
          >
            <v-icon
              class="mr-5"
              size="large"
              start
            >
              mdi-pencil-outline
            </v-icon>
            Create new Cluster
          </v-btn>
        </v-col>
      </v-row>
    </v-container>
    <v-card-actions class="justify-end">
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
import ClusterCreateDialogPage from "@/components/clusters/dialogs/ClustersCreateDialogPage";

export default {
    name: 'ResourcesCreateDialogPageStart',
    components: { },
    props: {
      show: {
        type: Boolean,
        default: false
      }
    },
    enums: {
      ClusterCreateDialogPage,
    },
    mounted() {
        this.$emit('title-changed', 'Add or create a new Cluster')
    },
    methods: {
      },
    }
</script>
