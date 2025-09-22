<script setup lang="ts" >
import { ref, computed } from 'vue';
import OverviewHeading from "@/components/base/OverviewHeading.vue";
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import DiscoveredResourcesTable from "@/components/discovery/DiscoveredResourcesTable.vue";
import DiscoverDialog from "@/components/discovery/DiscoverDialog.vue";
import { useDiscoveryStore } from "@/stores/discoveryStore";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import OnboardingDialog from "@/components/discovery/OnboardingDialog.vue";
import {storeToRefs} from "pinia";
import {useToast} from "vue-toast-notification";

const $toast = useToast();

const discoveryStore = useDiscoveryStore();
const { drivers, discoveredResources } = storeToRefs(discoveryStore);

const showDiscoverDialog = ref(false);
const showOnboardingDialog = ref(false);
const selectedDiscoveredResourceIds = ref<string[]>([]);

function triggerDiscovery(driver, filterValues: any, optionValues: any) {
  showDiscoverDialog.value = false;
  const discoveryRequest = {
    filterValues,
    optionValues
  };
  ResourceManagementClient.discoveryApi.discover(driver.instanceId, discoveryRequest);
  $toast.info(`Discovery job started by driver '${driver.instanceId}'`);
}

function triggerOnboarding() {
  showOnboardingDialog.value = false;
  selectedDiscoveredResourceIds.value = [];
  discoveryStore.updateStore();
}

function onSelectedDiscoveredResourcesChanged(ids: string[]) {
  selectedDiscoveredResourceIds.value = ids;
}
</script>

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
              :selected-discovered-resource-ids="selectedDiscoveredResourceIds"
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
        :discovered-resources-result-ids="selectedDiscoveredResourceIds"
        @canceled="showOnboardingDialog = false"
        @completed="triggerOnboarding"
    />
  </div>
</template>