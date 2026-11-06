<template>
  <base-material-card>
    <template #heading>
      <overview-heading text="Profilers" />
    </template>

    <no-item-available-note
      v-if="profilers.length === 0"
      item="Profiler"
    />

    <v-data-table
      v-else
      :headers="headers"
      :items="profilers"
      item-value="id"
    >
      <template #item.actions="{ item }">
        <v-btn
          class="mr-2"
          prepend-icon="mdi-pencil"
          size="small"
          @click.stop="emit('edit', item)"
        >
          Edit
        </v-btn>
        <v-btn
          prepend-icon="mdi-delete"
          size="small"
          color="error"
          @click.stop="profilerToDelete = item"
        >
          Delete
        </v-btn>
      </template>
    </v-data-table>

    <ConfirmDialog
      :show="profilerToDelete !== null"
      attention
      title="Delete profiler"
      :text="`Delete profiler '${profilerToDelete?.name}'? Its AWX job template and project are removed too, unless another profiler uses them.`"
      confirm-button-label="Delete"
      cancel-button-label="Cancel"
      :confirm-loading="deleting"
      @canceled="profilerToDelete = null"
      @confirmed="onDeleteConfirmed"
    />
  </base-material-card>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useToast } from 'vue-toast-notification';
import logRequestError from '@/api/restApiHelper';
import type { Profiler } from '@/api/resource-management/client';
import ConfirmDialog from '@/components/base/ConfirmDialog.vue';
import NoItemAvailableNote from '@/components/base/NoItemAvailableNote.vue';
import OverviewHeading from '@/components/base/OverviewHeading.vue';
import { useProfilersStore } from '@/stores/profilersStore';

const emit = defineEmits<{ (e: 'edit', profiler: Profiler): void }>();

const $toast = useToast();
const profilersStore = useProfilersStore();

const profilerToDelete = ref<Profiler | null>(null);
const deleting = ref(false);

const profilers = computed(() => profilersStore.profilers);

const headers = [
  { title: 'Name', key: 'name' },
  { title: 'Repository', key: 'action.awxRepo' },
  { title: 'Branch', key: 'action.awxBranch' },
  { title: 'Playbook', key: 'action.playbook' },
  { title: '', key: 'actions', sortable: false },
];

async function onDeleteConfirmed() {
  const profiler = profilerToDelete.value;
  if (!profiler) {
    return;
  }
  deleting.value = true;
  try {
    await profilersStore.deleteProfiler(profiler.id);
    $toast.success('Profiler deleted');
  } catch (e) {
    logRequestError(e);
    $toast.error('Failed to delete profiler');
  } finally {
    deleting.value = false;
    profilerToDelete.value = null;
  }
}
</script>
