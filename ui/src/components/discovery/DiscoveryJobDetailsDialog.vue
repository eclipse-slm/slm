<script setup lang="ts">
import {toRef, computed, ref} from "vue";
import {useDiscoveryStore} from "@/stores/discoveryStore";
import {storeToRefs} from "pinia";
import CustomDialog from "@/components/base/CustomDialog.vue";
import RowWithLabel from "@/components/base/RowWithLabel.vue";

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  discoveryJobId: {
    type: String,
  }
});

const emit = defineEmits(['canceled', 'completed']);

const discoveryStore = useDiscoveryStore();
const { discoveryJobById } = storeToRefs(discoveryStore);

const active = toRef(props, 'show');

const tableHeaders = [
  { title: 'Name', key: 'name', value: 'name' },
  { title: 'Product', key: 'productName', value: 'productName' },
  { title: 'Manufacturer', key: 'manufacturerName', value: 'manufacturerName' },
  { title: 'Serial number', key: 'serialNumber', value: 'serialNumber' },
  { title: 'IP address', key: 'ipAddress', value: 'ipAddress' },
  { title: 'MAC address ', key: 'macAddress', value: 'macAddress' },
  { title: 'Firmware version', key: 'firmwareVersion', value: 'firmwareVersion' },
];

const discoveryJob = computed<DiscoveryJob | undefined>(() =>
    discoveryJobById.value(props.discoveryJobId)
);

function closeDialog() {
  emit('canceled');
}
</script>

<template>
  <CustomDialog
      :show="active"
      title="Discovery job details"
      cancel-button-label="Cancel"
      width="50%"
      @canceled="closeDialog"
  >
    <template #content>
      <v-container v-if="active">
        <RowWithLabel label="Discovered resources"></RowWithLabel>
        <v-data-table
            :headers="tableHeaders"
            :items="discoveryJob.discoveryResult"
            hide-default-footer
            items-per-page="-1"
        >
        </v-data-table>
      </v-container>
    </template>
  </CustomDialog>
</template>
