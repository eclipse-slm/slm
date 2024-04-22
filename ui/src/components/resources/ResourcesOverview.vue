<template>
  <div>
    <base-material-card
      class="px-5 py-3"
    >
      <template #heading>
        <overview-heading text="Devices" />
      </template>

      <no-item-available-note
        v-if="!resources.length && !clusters.length"
        item="Resource and Cluster"
      />

      <v-card-text v-else>
        <v-row>
          <v-col cols="4">
            <v-text-field
              v-model="searchResources"
              label="Search Resources"
              append-icon="search"
              clearable
            />
          </v-col>
        </v-row>

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

    <v-fab-transition>
      <v-btn
        v-show="!showCreateButton"
        id="resources-button-add-resource"
        class="mb-10 elevation-15"
        color="primary"
        absolute
        location="bottom right"
        @click="showCreateDialog = true"
      >
        <v-icon size="large">
          mdi-plus
        </v-icon>
      </v-btn>
    </v-fab-transition>

    <resources-create-dialog
      :show="showCreateDialog"
      @canceled="showCreateDialog = false"
    />
  </div>
</template>

<script>
  import { mapGetters, mapActions } from 'vuex'
  import ResourcesInfoDialog from '@/components/resources/dialogs/ResourcesInfoDialog'
  import ResourcesCreateDialog from '@/components/resources/dialogs/create/ResourcesCreateDialog'
  import ResourcesTableSingleHosts from '@/components/resources/ResourcesTableSingleHosts'
  import ResourcesTableClusters from '@/components/resources/ResourcesTableClusters'
  import OverviewHeading from "@/components/base/OverviewHeading.vue";
  import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";

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
    data () {
      return {
        selectedResource: null,
        showCreateDialog: false,
        showDeleteDialog: false,
        showCreateButton: false,
      }
    },
    computed: {
      ...mapGetters([
        'resources',
        'clusters',
      ]),
      searchResources: {
        get () {
          return this.$store.state.resourcesStore.searchResources
        },
        set (value) {
          this.$store.commit('SET_SEARCH_RESOURCES', value)
        },
      },
    },
    mounted () {
      this.$store.dispatch('getDeploymentCapabilities')
    },
    methods: {
      ...mapActions([
        'getResourcesFromBackend',
      ]),

      onResourceSelected (resource) {
        this.selectedResource = resource
      },
    },
  }
</script>
