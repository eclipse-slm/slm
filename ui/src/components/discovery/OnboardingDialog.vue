<template>
  <confirm-dialog
      :show="active"
      title="Onboarding"
      cancel-button-label="Cancel"
      confirm-button-label="Add"
      width="35%"
      @canceled="closeDialog"
      @confirmed="onConfirmButtonClicked"
  >
    <template #content>
      <v-container>
      Do you want to onboard the following discovered devices?
      <v-data-table
          :headers="tableHeaders"
          :items="discoveredResources"
          hide-default-footer
          items-per-page="-1"
      >
      </v-data-table>
      </v-container>
    </template>
  </confirm-dialog>
</template>

<script setup lang="ts">
import { toRef, computed } from "vue";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import {useDiscoveryStore} from "@/stores/discoveryStore";
import {storeToRefs} from "pinia";
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  discoveredResourcesResultIds: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['canceled', 'completed']);

const discoveryStore = useDiscoveryStore();
const { discoveredResourceByResultId } = storeToRefs(discoveryStore);

const active = toRef(props, 'show');

const tableHeaders = [
  { title: "Name", value: "name" },
  { title: "Product", value: "productName" },
  { title: "Manufacturer", value: "manufacturerName" },
  { title: "Serial number ", value: "serialNumber" },
];

const discoveredResources = computed(() =>
    props.discoveredResourcesResultIds
        .map((id: string) => discoveredResourceByResultId.value(id))
        .filter(Boolean)
);

function closeDialog() {
  emit('canceled');
}

function onConfirmButtonClicked() {
  ResourceManagementClient.discoveryApi
      .onboardDiscoveredResources({ 'resultIds': props.discoveredResourcesResultIds })
      .then(() => {
        emit('completed');
      });
}
</script>
