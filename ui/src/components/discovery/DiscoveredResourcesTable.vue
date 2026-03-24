<template>
  <v-container fluid>
    <v-row
      dense
      class="ml-8 mb-8"
      align="center"
    >
      <v-text-field
        v-model="searchDiscoveredResources"
        label="Search discovered resources"
        append-inner-icon="mdi-magnify"
        clearable
        variant="underlined"
      />
      <v-spacer />
      <v-tooltip
        text="Show ignored resources"
        open-delay="1000"
      >
        <template #activator="{ props }">
          <v-switch
            v-model="showIgnoredResources"
            v-bind="props"
            class="mx-8"
            color="primary"
            prepend-icon="mdi-eye-off-outline"
            @update:model-value="onFilterChanged"
          />
        </template>
      </v-tooltip>

      <v-tooltip
        text="Show only latest jobs of drivers"
        open-delay="1000"
      >
        <template #activator="{ props }">
          <v-switch
            v-model="showOnlyLatestJobsOfDrivers"
            v-bind="props"
            class="mx-8"
            color="primary"
            prepend-icon="mdi-history"
            @update:model-value="onFilterChanged"
          />
        </template>
      </v-tooltip>
      <v-tooltip
        text="Remove duplicates"
        open-delay="1000"
      >
        <template #activator="{ props }">
          <v-switch
            v-model="removeDuplicates"
            v-bind="props"
            class="mx-8"
            color="primary"
            prepend-icon="mdi-content-duplicate"
            @update:model-value="onFilterChanged"
          />
        </template>
      </v-tooltip>
    </v-row>
    <v-data-table
      id="table-discovered-resources"
      :model-value="props.selectedDiscoveredResourceIds"
      @update:model-value="val => emit('selectedDiscoveredResourcesChanged', val)"
      :headers="tableHeaders"
      :items="discoveredResources"
      :search="searchDiscoveredResources"
      item-key="resultId"
      item-value="resultId"
      show-select
      :row-props="colorRowItem"
      :loading="apiState === ApiState.LOADING || apiState === ApiState.UPDATING"
      @click:row="onRowClick"
    >
      <template
        #item.actions="{ item }"
      >
        <action-button
          :type="ActionButtonType.CUSTOM"
          color="secondary"
          :icon="item.ignored ? 'mdi-eye-outline' : 'mdi-eye-off-outline'"
          @click="onIgnoreResourceButtonClicked(item)"
        />
      </template>
    </v-data-table>

    <confirm-dialog
      :show="confirmIgnoreResourceDialog"
      title="Ignore discovered resource"
      :text="resourceSelectedForDeleted?.ignored ? `Do you want to unignore the discovered resource '${resourceSelectedForDeleted?.name}'?`
        : `Do you want to ignore the discovered resource '${resourceSelectedForDeleted?.name}'?`"
      @confirmed="onIgnoreResourceDialogConfirmed"
      @canceled="confirmIgnoreResourceDialog = false"
    />
  </v-container>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useDiscoveryStore } from "@/stores/discoveryStore";
import {storeToRefs} from "pinia";
import ApiState from "@/api/apiState";
import ActionButton from "@/components/base/ActionButton.vue";
import {ActionButtonType} from "@/components/base/ActionButtonType";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";

const props = defineProps({
  selectedDiscoveredResourceIds: Array
});

const emit = defineEmits(['selectedDiscoveredResourcesChanged'])

const discoveryStore = useDiscoveryStore();
const { discoveredResources, apiState } = storeToRefs(discoveryStore);

const searchDiscoveredResources = ref(undefined);
const showIgnoredResources = ref(false);
const showOnlyLatestJobsOfDrivers = ref(false);
const removeDuplicates = ref(false);

const tableHeaders = [
  { title: 'Name', key: 'name', value: 'name' },
  { title: 'Product', key: 'productName', value: 'productName' },
  { title: 'Manufacturer', key: 'manufacturerName', value: 'manufacturerName' },
  { title: 'Serial number', key: 'serialNumber', value: 'serialNumber' },
  { title: 'IP address', key: 'ipAddress', value: 'ipAddress' },
  { title: 'MAC address ', key: 'macAddress', value: 'macAddress' },
  { title: 'Firmware version', key: 'firmwareVersion', value: 'firmwareVersion' },
  { title: '', value: 'actions', sortable: false },
];

const onRowClick = (click, row) => {
  row.toggleSelect({ value: row.item.resultId });
};

const colorRowItem = (row) => {
  if (props.selectedDiscoveredResourceIds.includes(row.item.resultId)) {
    return { class: 'v-data-table__selected' };
  }
};

const onFilterChanged = () => {
  discoveryStore.getDiscoveredResources(
    removeDuplicates.value,
    showOnlyLatestJobsOfDrivers.value,
    showIgnoredResources.value
  )
};

const resourceSelectedForDeleted = ref(undefined)
const confirmIgnoreResourceDialog = ref(false)
const onIgnoreResourceButtonClicked = (item) => {
  confirmIgnoreResourceDialog.value = true;
  console.log(item)
  resourceSelectedForDeleted.value = item;
};
const onIgnoreResourceDialogConfirmed = () => {
  confirmIgnoreResourceDialog.value = false;
  ResourceManagementClient.discoveryApi.ignoreDiscoveredResource(resourceSelectedForDeleted.value.resultId, !resourceSelectedForDeleted.value.ignored);
  discoveryStore.getDiscoveredResources(
      removeDuplicates.value,
      showOnlyLatestJobsOfDrivers.value,
      showIgnoredResources.value
  )
};

</script>

<style>
tr.v-data-table__selected {
  background: rgb(var(--v-theme-secondary), 0.05) !important;
}
</style>