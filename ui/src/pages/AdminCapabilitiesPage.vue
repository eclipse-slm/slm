<template>
  <v-container
    id="admin-capabilities"
    fluid
    tag="section"
  >
    <div
      v-if="apiStateLoading"
      class="text-center"
    >
      <v-progress-circular
        :size="70"
        :width="7"
        color="primary"
        indeterminate
      />
    </div>
    <div v-if="apiStateError">
      Error
    </div>

    <div v-if="apiStateLoaded">
      <CapabilitiesTable />
      <CapabilityCreateDialog
        :show="showCreateDialog"
        @canceled="showCreateDialog = false"
        @confirmed="showCreateDialog = false"
      />
      <v-fab
        icon="mdi-plus"
        class="mx-4"
        elevation="15"
        color="primary"
        location="right bottom"
        :app="true"
        @click="showCreateDialog = true"
      />
    </div>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import ApiState from '@/api/apiState';
import CapabilitiesTable from '@/components/capabilities/CapabilitiesTable.vue';
import CapabilityCreateDialog from '@/components/capabilities/CapabilityCreateDialog.vue';
import { useCapabilitiesStore } from '@/stores/capabilitiesStore';

const capabilitiesStore = useCapabilitiesStore();

const showCreateDialog = ref(false);

const apiStateLoaded = computed(() => capabilitiesStore.apiState === ApiState.LOADED || capabilitiesStore.apiState === ApiState.UPDATING);
const apiStateLoading = computed(() => capabilitiesStore.apiState === ApiState.LOADING || capabilitiesStore.apiState === ApiState.INIT);
const apiStateError = computed(() => capabilitiesStore.apiState === ApiState.ERROR);

onMounted(() => {
  capabilitiesStore.updateStore();
});
</script>
