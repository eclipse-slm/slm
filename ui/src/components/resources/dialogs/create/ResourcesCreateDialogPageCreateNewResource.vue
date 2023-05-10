<template>
  <v-card>
    <v-container>
      <v-row class="my-3">
        <v-col class="text-center">
          <v-btn
            color="secondary"
            x-large
            tile
            :disabled="virtualResourceProviders.length === 0"
            @click="$emit('page-changed', ResourcesCreateDialogPage.CREATE_RESOURCE_HOST)"
          >
            <v-icon
              class="mr-5 ml-2"
              x-large
              left
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
            x-large
            tile
            :disabled="availableClusterTypes.length === 0"
            @click="$emit('page-changed', ResourcesCreateDialogPage.CREATE_RESOURCE_CLUSTER)"
          >
            <v-icon
              class="mr-5 ml-2"
              x-large
              left
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
        text
        @click="$emit('page-changed', ResourcesCreateDialogPage.START)"
      >
        Back
      </v-btn>
      <v-spacer />
      <v-btn
        text
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

  export default {
    name: 'ResourcesCreateDialogPageCreateNewResource',
    enums: {
      ResourcesCreateDialogPage,
    },
    mounted() {
      this.$emit('title-changed', 'Create new resource')
    },
    computed: {
      ...mapGetters(['virtualResourceProviders', 'availableClusterTypes']),
    },
  }
</script>
