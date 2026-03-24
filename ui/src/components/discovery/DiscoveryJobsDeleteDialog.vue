<script setup lang="ts">
import {toRef, computed, ref} from "vue";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import {useDiscoveryStore} from "@/stores/discoveryStore";
import {storeToRefs} from "pinia";
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";
import logRequestError from "@/api/restApiHelper";

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  discoveryJobIds: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['canceled', 'completed']);

const discoveryStore = useDiscoveryStore();
const { discoveryJobById } = storeToRefs(discoveryStore);

const active = toRef(props, 'show');
const loading = ref(false)

const tableHeaders = [
  { title: "ID", value: "id" },
  { title: "Driver", value: "driverId" },
];

const discoveryJobs = computed(() =>
    props.discoveryJobIds
        .map((id) => discoveryJobById.value(id))
        .filter(Boolean)
);

function closeDialog() {
  emit('canceled');
}

async function onConfirmButtonClicked() {
  loading.value = true;
  await Promise.allSettled(
      props.discoveryJobIds.map(discoveryJobId =>
          ResourceManagementClient.discoveryApi.deleteDiscoveryJob(discoveryJobId).then(() => {
          }).catch(logRequestError)
      )
  );
  loading.value = false;
  discoveryStore.updateStore()
  emit('completed');
}
</script>


<template>
  <confirm-dialog
      :show="active"
      title="Delete discovery jobs"
      cancel-button-label="Cancel"
      confirm-button-label="Delete"
      width="35%"
      :attention="true"
      @canceled="closeDialog"
      @confirmed="onConfirmButtonClicked"
  >
    <template #content>
      <v-container>
        Do you want to delete the selected discovery jobs?
        <v-data-table
            :headers="tableHeaders"
            :items="discoveryJobs"
            hide-default-footer
            items-per-page="-1"
        >
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