<template>
  <confirm-dialog
      :show="active"
      title="Delete resources"
      cancel-button-label="Cancel"
      confirm-button-label="Delete"
      width="35%"
      :attention="true"
      @canceled="closeDialog"
      @confirmed="onConfirmButtonClicked"
  >
    <template #content>
      <v-container>
        Do you want to delete the selected resources?
        <v-data-table
            :headers="tableHeaders"
            :items="resources"
            hide-default-footer
            items-per-page="-1"
        >
          <template #item.product="{ item }">
            <div>
              {{ DeviceUtils.getProduct(item.id) }}
            </div>
          </template>

          <template #item.manufacturer="{ item }">
            <div>
              {{ DeviceUtils.getManufacturer(item.id) }}
            </div>
          </template>
        </v-data-table>
        <div
            v-if="loading"
            style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; background: rgba(255,255,255,0.7); display: flex; align-items: center; justify-content: center; z-index: 10;"
        >
          <v-progress-circular v-if="loading" indeterminate color="primary" size="64" />
        </div>
      </v-container>
    </template>
  </confirm-dialog>
</template>

<script setup lang="ts">
import {toRef, computed, ref} from "vue";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import {useDiscoveryStore} from "@/stores/discoveryStore";
import {storeToRefs} from "pinia";
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import DeviceUtils from "@/utils/deviceUtils";
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import logRequestError from "@/api/restApiHelper";

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  resourceIds: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['canceled', 'completed']);

const resourceDevicesStore = useResourceDevicesStore();
const { resourceById } = storeToRefs(resourceDevicesStore);

const active = toRef(props, 'show');
const loading = ref(false)

const tableHeaders = [
  { title: "Product", value: "product" },
  { title: "Manufacturer", value: "manufacturer" },
  { title: "Hostname", value: "hostname" },
  { title: "IP ", value: "ip" },
];

const resources = computed(() =>
    props.resourceIds
        .map((id) => resourceById.value(id))
        .filter(Boolean)
);

function closeDialog() {
  emit('canceled');
}

async function onConfirmButtonClicked() {
  loading.value = true;
  await Promise.allSettled(
      props.resourceIds.map(resourceId =>
          ResourceManagementClient.resourcesApi.deleteResource(resourceId).then(() => {
          }).catch(logRequestError)
      )
  );
  loading.value = false;
  emit('completed');
}
</script>
