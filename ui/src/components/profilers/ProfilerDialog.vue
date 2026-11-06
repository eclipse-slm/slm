<script setup lang="ts">
import { computed, ref, toRef, watch } from 'vue';
import { useToast } from 'vue-toast-notification';
import logRequestError from '@/api/restApiHelper';
import type { Profiler } from '@/api/resource-management/client';
import ConfirmDialog from '@/components/base/ConfirmDialog.vue';
import ProfilerForm from '@/components/profilers/ProfilerForm.vue';
import {
  buildProfilerRequest,
  newProfilerFormData,
  profilerToFormData,
} from '@/components/profilers/profilerForm';
import type { ProfilerForm as ProfilerFormState } from './profilerForm';
import { useProfilersStore } from '@/stores/profilersStore';

const props = defineProps<{ show: boolean; profiler: Profiler | null }>();
const emit = defineEmits(['canceled', 'confirmed']);

const $toast = useToast();
const profilersStore = useProfilersStore();

const active = toRef(props, 'show');
const isEdit = computed(() => props.profiler !== null);
const loading = ref(false);
const formKey = ref(0);
const form = ref<ProfilerFormState>({ isFormValid: false, formData: newProfilerFormData() });

function loadForm() {
  formKey.value++; // remount the form to reset vee-validate state
  form.value = {
    isFormValid: false,
    formData: props.profiler ? profilerToFormData(props.profiler) : newProfilerFormData(),
  };
}

watch(() => props.show, (show) => {
  if (show) loadForm();
}, { immediate: true });

function closeDialog() {
  emit('canceled');
}

async function save() {
  if (!form.value.isFormValid) {
    $toast.error('Please fill in all required fields correctly');
    return;
  }
  loading.value = true;
  try {
    const profiler = props.profiler;
    if (profiler) {
      await profilersStore.updateProfiler(
        profiler.id,
        buildProfilerRequest(form.value.formData, profiler.id, profiler),
      );
      $toast.info('Profiler updated successfully');
    } else {
      await profilersStore.createProfiler(
        buildProfilerRequest(form.value.formData, globalThis.crypto.randomUUID()),
      );
      $toast.info('Profiler created successfully');
    }
    emit('confirmed');
  } catch (e) {
    logRequestError(e);
    $toast.error(isEdit.value ? 'Failed to update profiler' : 'Failed to create profiler');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <ConfirmDialog
    :show="active"
    :title="isEdit ? 'Edit profiler' : 'Add profiler'"
    width="800px"
    cancel-button-label="Cancel"
    :confirm-button-label="isEdit ? 'Save changes' : 'Create profiler'"
    :confirm-button-disabled="!form.isFormValid"
    :confirm-loading="loading"
    @canceled="closeDialog"
    @confirmed="save"
  >
    <template #content>
      <ProfilerForm
        :key="formKey"
        v-model="form"
        :edit="isEdit"
        :profiler="profiler"
      />
    </template>
  </ConfirmDialog>
</template>
