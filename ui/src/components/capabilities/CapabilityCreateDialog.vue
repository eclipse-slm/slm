<script setup lang="ts">
import { ref, toRef } from 'vue';
import { useToast } from 'vue-toast-notification';
import logRequestError from '@/api/restApiHelper';
import ConfirmDialog from '@/components/base/ConfirmDialog.vue';
import CapabilityCreateForm from '@/components/capabilities/CapabilityCreateForm.vue';
import { buildCapabilityRequest, newCapabilityFormData } from '@/components/capabilities/capabilityForm';
import type { CapabilityForm } from '@/components/capabilities/capabilityForm';
import { useCapabilitiesStore } from '@/stores/capabilitiesStore';

const props = defineProps({
  show: {
    type: Boolean,
    default: false,
  },
});
const emit = defineEmits(['canceled', 'confirmed']);

const $toast = useToast();
const capabilitiesStore = useCapabilitiesStore();

const active = toRef(props, 'show');
const loading = ref(false);
const form = ref<CapabilityForm>({ isFormValid: false, formData: newCapabilityFormData() });

function clearDialog() {
  form.value = { isFormValid: false, formData: newCapabilityFormData() };
}

function closeDialog() {
  clearDialog();
  emit('canceled');
}

async function createCapability() {
  if (!form.value.isFormValid) {
    $toast.error('Please fill in all required fields correctly');
    return;
  }
  loading.value = true;
  try {
    await capabilitiesStore.createCapability(
      buildCapabilityRequest(form.value.formData, globalThis.crypto?.randomUUID?.()),
    );
    $toast.info('Capability created successfully');
    clearDialog();
    emit('confirmed');
  } catch (e) {
    logRequestError(e);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <ConfirmDialog
    :show="active"
    title="Add capability"
    width="800px"
    cancel-button-label="Cancel"
    confirm-button-label="Create capability"
    :confirm-button-disabled="!form.isFormValid"
    :confirm-loading="loading"
    @canceled="closeDialog"
    @confirmed="createCapability"
  >
    <template #content>
      <CapabilityCreateForm v-model="form" />
    </template>
  </ConfirmDialog>
</template>
