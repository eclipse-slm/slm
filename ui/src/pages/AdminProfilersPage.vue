<template>
  <v-container
    id="admin-profilers"
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
      <ProfilersTable @edit="onEdit" />
      <ProfilerDialog
        :show="showDialog"
        :profiler="selectedProfiler"
        @canceled="showDialog = false"
        @confirmed="showDialog = false"
      />
      <v-fab
        icon="mdi-plus"
        class="mx-4"
        elevation="15"
        color="primary"
        location="right bottom"
        :app="true"
        @click="onCreate"
      />
    </div>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import ApiState from '@/api/apiState';
import type { Profiler } from '@/api/resource-management/client';
import ProfilerDialog from '@/components/profilers/ProfilerDialog.vue';
import ProfilersTable from '@/components/profilers/ProfilersTable.vue';
import { useProfilersStore } from '@/stores/profilersStore';

const profilersStore = useProfilersStore();

const showDialog = ref(false);
const selectedProfiler = ref<Profiler | null>(null);

const apiStateLoaded = computed(() => profilersStore.apiState === ApiState.LOADED || profilersStore.apiState === ApiState.UPDATING);
const apiStateLoading = computed(() => profilersStore.apiState === ApiState.LOADING || profilersStore.apiState === ApiState.INIT);
const apiStateError = computed(() => profilersStore.apiState === ApiState.ERROR);

function onCreate() {
  selectedProfiler.value = null;
  showDialog.value = true;
}

function onEdit(profiler: Profiler) {
  selectedProfiler.value = profiler;
  showDialog.value = true;
}

onMounted(() => {
  profilersStore.updateStore();
});
</script>
