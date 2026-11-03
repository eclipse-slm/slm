<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useToast } from 'vue-toast-notification';
import ResourceManagementClient from '@/api/resource-management/resource-management-client';
import logRequestError from '@/api/restApiHelper';
import ConfirmDialog from '@/components/base/ConfirmDialog.vue';
import { capabilityToFormData, DEFAULT_BRANCH, importErrorMessage } from './capabilityForm';
import type { CapabilityFormData, ImportedCapability } from './capabilityForm';

const DEFAULT_FILE_PATH = 'capability.json';

const props = defineProps<{ show: boolean }>();
const emit = defineEmits<{
  (e: 'imported', value: CapabilityFormData): void;
  (e: 'canceled'): void;
}>();

const $toast = useToast();

const repositoryUrl = ref('');
const branch = ref(DEFAULT_BRANCH);
const filePath = ref(DEFAULT_FILE_PATH);
const loading = ref(false);

const urlRule = (v: string) => /^https?:\/\/\S+$/i.test((v ?? '').trim()) || 'Must start with http:// or https://';
const isUrlValid = computed(() => urlRule(repositoryUrl.value) === true);

// Start from the defaults each time the dialog opens
watch(() => props.show, (open) => {
  if (!open) return;
  repositoryUrl.value = '';
  branch.value = DEFAULT_BRANCH;
  filePath.value = DEFAULT_FILE_PATH;
});

async function importCapability() {
  if (!isUrlValid.value) return;
  loading.value = true;
  try {
    const response = await ResourceManagementClient.capabilityApi.importCapabilityFromGit({
      repositoryUrl: repositoryUrl.value.trim(),
      branch: branch.value.trim(),
      filePath: filePath.value.trim(),
    });
    emit('imported', capabilityToFormData(response.data as ImportedCapability));
  } catch (e) {
    logRequestError(e);
    $toast.error(importErrorMessage(e));
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <ConfirmDialog
    :show="show"
    title="Import capability"
    width="600px"
    cancel-button-label="Cancel"
    confirm-button-label="Import"
    :confirm-button-disabled="!isUrlValid"
    :confirm-loading="loading"
    @canceled="emit('canceled')"
    @confirmed="importCapability"
  >
    <template #content>
      <v-text-field
        v-model="repositoryUrl"
        label="Repository URL"
        required
        :rules="[urlRule]"
      />
      <v-text-field
        v-model="branch"
        label="Branch or tag"
      />
      <v-text-field
        v-model="filePath"
        label="File path"
        hint="Path inside the repository"
        persistent-hint
      />
    </template>
  </ConfirmDialog>
</template>
