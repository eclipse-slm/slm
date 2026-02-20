<script setup lang="ts">
import { ref, watch, computed, PropType } from 'vue';
import CustomDialog from '@/components/base/CustomDialog.vue';
import PlatformManagementClient from '@/api/platform-management/platform-management-client';
import { useToast } from 'vue-toast-notification';
import logRequestError from '@/api/restApiHelper';
import { useUserStore } from '@/stores/userStore';
import { Credential, CredentialCreateRequest } from '@/api/platform-management/client';
import CredentialForm from '@/components/credentials/CredentialForm.vue';
import { CredentialFormData } from '@/components/credentials/CredentialTypes';

const $toast = useToast();

const props = defineProps({
  show: { type: Boolean, default: false },
  // list of scopes for this dialog (injected by parent)
  scopes: { type: Array as PropType<string[]>, required: false },
  // entity links to associate with the credential
  entityLinks: { type: Array as PropType<Array<{ entityType: string; entityId: string }>>, required: false },
  // optional credential id; if not provided, a UUID will be generated
  credentialId: { type: String, required: false }
});

const emit = defineEmits(['created', 'canceled']);

const dialogActive = ref(false);
watch(() => props.show, (v) => { dialogActive.value = v; });

const userStore = useUserStore();

// Credential form child
const credentialFormData = ref<CredentialFormData | undefined>(undefined);

const submitting = ref(false);
const apiError = ref<string | null>(null);

const canSubmit = computed(() => {
  if (submitting.value) return false;
  return !!credentialFormData.value?.isFormValid;
});

function clearForm() {
  credentialFormData.value = undefined;
  apiError.value = null;
  submitting.value = false;
}

async function onSubmit() {
  apiError.value = null;
  submitting.value = true;

  if (!credentialFormData.value?.isFormValid || !credentialFormData.value.formData.data) {
    apiError.value = 'Form is not valid';
    submitting.value = false;
    return;
  }

  const credentialId = props.credentialId ?? (globalThis.crypto?.randomUUID?.() ?? undefined);
  const request: CredentialCreateRequest = {
    entityLinks: props.entityLinks,
    fullPathOwnerGroupId: userStore.fullPathUserGroupId,
    credential: {
      id: credentialId,
      name: credentialFormData.value.formData.credentialName,
      scopesRaw: props.scopes,
      data: credentialFormData.value.formData.data,
    } as Credential,
  } as CredentialCreateRequest;

  await PlatformManagementClient.credentialsApi.createCredential(request).then(() => {
    emit('created');
    dialogActive.value = false;
    clearForm();
  }).catch((err) => {
    logRequestError(err);
    $toast.error('Error creating credential: ' + (err?.message ?? 'Unknown error'));
  }).finally(() => {
    submitting.value = false;
  });
}

function onCancel() {
  emit('canceled');
  dialogActive.value = false;
  clearForm();
}
</script>

<template>
  <CustomDialog :show="dialogActive" title="Create credential" :width="'30%'" @canceled="onCancel">
    <template #content>
      <div>
        <v-alert v-if="apiError" type="error" variant="tonal">{{ apiError }}</v-alert>

        <!-- Credential input -->
        <CredentialForm
          v-model="credentialFormData"
          :allow-existing="false"
        />
      </div>
    </template>

    <template #actions>
      <v-row class="justify-end">
        <v-col cols="2">
          <v-btn variant="text" @click="onCancel">Cancel</v-btn>
        </v-col>
        <v-col cols="8"></v-col>
        <v-col cols="2">
          <v-btn :disabled="!canSubmit" color="primary" @click="onSubmit">
            <v-progress-circular v-if="submitting" indeterminate size="16" width="2" />
            <span v-else>Create</span>
          </v-btn>
        </v-col>
      </v-row>
    </template>
  </CustomDialog>
</template>

<style scoped>
</style>
