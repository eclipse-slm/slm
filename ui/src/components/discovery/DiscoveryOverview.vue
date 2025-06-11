<template>
  <div>
    <base-material-card>
      <template #heading>
        <overview-heading text="Discovery" />
      </template>



      <v-card-text>
        <no-item-available-note
          v-if="!discoveredResources.length"
          item="discovered resources"
        />

        <v-row v-else>
          <discovered-resources-table
            v-if="discoveredResources.length > 0"
            class="mt-0 flex"
            @selected-discovered-resources-changed="onSelectedDiscoveredResourcesChanged"
          />
        </v-row>
        <v-fab
          icon="mdi-magnify"
          elevation="15"
          color="primary"
          location="right bottom"
          absolute
          offset
          @click="showDiscoverDialog = true"
        />
        <v-fab
          icon="mdi-database-plus"
          class="mx-16"
          color="primary"
          location="right bottom"
          absolute
          offset
          appear
          :active="selectedDiscoveredResourceIds.length > 0"
          @click="showOnboardingDialog = true"
        />
      </v-card-text>
    </base-material-card>

    <discover-dialog
      :show="showDiscoverDialog"
      :drivers="drivers"
      @canceled="showDiscoverDialog = false"
      @completed="triggerDiscovery"
    />
    <onboarding-dialog
      :show="showOnboardingDialog"
      :discovered-resources-ids="selectedDiscoveredResourceIds"
      @canceled="showOnboardingDialog = false"
      @completed="triggerOnboarding"
    />
  </div>
</template>

<script>
import OverviewHeading from "@/components/base/OverviewHeading.vue";
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import DiscoveredResourcesTable from "@/components/discovery/DiscoveredResourcesTable.vue";
import DiscoverDialog from "@/components/discovery/DiscoverDialog.vue";
import {useDiscoveryStore} from "@/stores/discoveryStore";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import OnboardingDialog from "@/components/discovery/OnboardingDialog.vue";

export default {
  name: 'DiscoveryOverview',
  components: {
    OverviewHeading,
    DiscoverDialog,
    OnboardingDialog,
    DiscoveredResourcesTable,
    NoItemAvailableNote,
  },
  setup() {
    const discoveryStore = useDiscoveryStore();
    return {discoveryStore};
  },
  data() {
    return {
      showDiscoverDialog: false,
      showOnboardingDialog: false,
      selectedDiscoveredResourceIds: [],
    }
  },
  computed: {
    drivers() {
      return this.discoveryStore.drivers
    },
    discoveredResources() {
      return this.discoveryStore.discoveredResources
    }
  },
  methods: {
    triggerDiscovery(driver, filterValues, optionValues) {
      this.showDiscoverDialog = false;
      var discoveryRequest = {
        'filterValues': filterValues,
        'optionValues': optionValues
      }

      ResourceManagementClient.discoveryApi.discover(driver.instanceId, discoveryRequest);
      this.$toast.info(`Scan started by driver '${driver.instanceId}'`)
    },
    triggerOnboarding() {
      this.showOnboardingDialog = false;
      this.discoveryStore.updateStore();
    },
    onSelectedDiscoveredResourcesChanged: function(selectedDiscoveredResourceIds) {
      this.selectedDiscoveredResourceIds = selectedDiscoveredResourceIds
    }
  }
}
</script>
