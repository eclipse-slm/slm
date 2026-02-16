<script setup lang="ts">

import ConfirmDialog from "@/components/base/ConfirmDialog.vue";
import {storeToRefs} from "pinia";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import {ref, toRef, computed, watch} from "vue";
import {useField} from "vee-validate";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";
import {useToast} from "vue-toast-notification";
import {
  ResourceCredentialScope,
  RemoteAccessCreateDTO,
  ConnectionType
} from "@/api/resource-management/client";
import {useUserStore} from "@/stores/userStore";
import CredentialForm from "@/components/credentials/CredentialForm.vue";
import {CredentialFormData} from "@/components/credentials/types";
import PlatformManagementClient from '@/api/platform-management/platform-management-client';
import { Credential, CredentialCreateRequest, CredentialDataType } from '@/api/platform-management/client';

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  resourceId: {
    type: String,
    required: true
  }
});

const emit = defineEmits(['canceled', 'confirmed']);

const $toast = useToast();

const active = toRef(props, 'show');

// Stores
const userStore = useUserStore();
const resourceDevicesStores = useResourceDevicesStore();
const { resourceConnectionTypes } = storeToRefs(resourceDevicesStores);

// Credential Form
const credentialFormRef = ref<any | null>(null);
const credentialFormData = ref<CredentialFormData | undefined>(undefined);
const remoteAccessUsername = ref('');
const showRemoteAccessUsername = computed(() => {
  const formData = credentialFormData.value;
  if (!formData) {
    return false;
  }
  if (formData.useExisting) {
    return formData.existingCredentialDataType === CredentialDataType.KeyPair;
  }
  return formData.data?.credentialDataType === CredentialDataType.KeyPair;
});

watch(showRemoteAccessUsername, (value) => {
  if (!value) {
    remoteAccessUsername.value = '';
    remoteAccessUsernameField.value = '';
  } else {
    remoteAccessUsernameField.value = remoteAccessUsername.value;
  }
});
watch(remoteAccessUsername, (value) => {
  remoteAccessUsernameField.value = value;
});

// Vee-Validate fields
const { value: connectionTypeField, errorMessage: connectionTypeError } = useField<string>('connectionType', (v: any) => v ? true : 'Connection Type is required');
const { value: connectionPortField, errorMessage: connectionPortError } = useField<number | string>('connectionPort', (v: any) => {
   if (v === undefined || v === null || v === '') return 'Connection Port is required';
   const n = Number(v);
   if (isNaN(n) || n < 1 || n > 65535) return 'Port must be a number between 1 and 65535';
   return true;
 });
const { value: remoteAccessUsernameField, errorMessage: remoteAccessUsernameError } = useField<string>(
  'remoteAccessUsername',
  (v: any) => {
    if (!showRemoteAccessUsername.value) return true;
    return v ? true : 'Username is required';
  }
);

// Initialize field values with defaults
 connectionTypeField.value = resourceConnectionTypes.value[0]?.name
 connectionPortField.value = resourceConnectionTypes.value[0]?.defaultPort;

// Validation state
const credentialFormValid = ref(false);
const otherFieldsValid = computed(() => {
  const baseValid = !connectionTypeError.value && !connectionPortError.value && !!connectionTypeField.value && connectionPortField.value !== '' && connectionPortField.value !== undefined;
  const usernameValid = !showRemoteAccessUsername.value || (!!remoteAccessUsernameField.value && !remoteAccessUsernameError.value);
  return baseValid && usernameValid;
});
const formValid = computed(() => credentialFormValid.value && otherFieldsValid.value);

async function createCredentialForRemoteAccess() {
  if (!credentialFormData.value?.data) {
    throw new Error('Credential data missing');
  }
  const credentialId = globalThis.crypto?.randomUUID?.();
  if (!credentialId) {
    throw new Error('Unable to generate credential id');
  }

  const request: CredentialCreateRequest = {
    entityLinks: [],
    fullPathOwnerGroupId: userStore.fullPathUserGroupId,
    credential: {
      id: credentialId,
      scopesRaw: [ResourceCredentialScope.RemoteAccess],
      data: credentialFormData.value.data,
    } as Credential,
  } as CredentialCreateRequest;

  await PlatformManagementClient.credentialsApi.createOrUpdateCredential(credentialId, request);
  return credentialId;
}

async function addRemoteAccessWithCredentialId(credentialId: string) {
   const remoteAccessCreateDTO = {
     fullPathOwnerGroupId: userStore.fullPathUserGroupId,
     credentialId: credentialId,
     username: showRemoteAccessUsername.value ? remoteAccessUsername.value : undefined,
     connectionType: connectionTypeField.value as ConnectionType,
     connectionPort: connectionPortField.value as number,
   } as RemoteAccessCreateDTO as any;

   return ResourceManagementClient.resourcesApi.addRemoteAccessForResource(
     props.resourceId,
     remoteAccessCreateDTO
   );
}

const confirmLoading = ref(false);

const onConfirmClicked = async () => {
  if (confirmLoading.value) {
    return;
  }
  confirmLoading.value = true;
  try {
    if (!credentialFormData.value?.isFormValid) {
      $toast.error('Credential form is not valid');
      return;
    }

    if (credentialFormData.value.useExisting) {
      const existingId = credentialFormData.value.existingCredentialId;
      if (!existingId) {
        $toast.error('Existing credential is required');
        return;
      }
      await addRemoteAccessWithCredentialId(existingId);
    } else {
      const createdCredentialId = await createCredentialForRemoteAccess();
      await addRemoteAccessWithCredentialId(createdCredentialId);
    }

    $toast.info("Remote access successfully added");
    clearForm();
    emit('confirmed');
  } catch (e) {
    $toast.error("Error adding remote access");
    logRequestError(e);
  } finally {
    confirmLoading.value = false;
  }
};

const clearForm = () => {
   connectionTypeField.value = resourceConnectionTypes.value[0]?.name
   connectionPortField.value = resourceConnectionTypes.value[0]?.defaultPort
   remoteAccessUsername.value = ''
   remoteAccessUsernameField.value = ''
   credentialFormData.value = undefined
   credentialFormRef.value?.clearForm?.();
}

const onCredentialFormChanged = (formData: CredentialFormData) => {
  credentialFormData.value = formData;
  credentialFormValid.value = !!formData?.isFormValid;
}
</script>

<template>
  <confirm-dialog
      :show="active"
      title="Add remote access"
      cancel-button-label="Cancel"
      confirm-button-label="Add"
      width="30%"
      :confirmButtonDisabled="!formValid"
      :confirm-loading="confirmLoading"
      @canceled="clearForm(); $emit('canceled');"
      @confirmed="onConfirmClicked"
  >
    <template #content>
      <v-row>
        <v-col cols="9">
            <v-select
                id="resource-select-connection-type"
                v-model="connectionTypeField"
                required
                label="Connection Type"
                prepend-icon="mdi-connection"
                :items="resourceConnectionTypes"
                item-title="prettyName"
                item-value="name"
                persistent-placeholder
                :error-messages="connectionTypeError"
            />
        </v-col>
        <v-col cols="3">
            <v-text-field
                v-model="connectionPortField"
                type="number"
                required
                label="Connection Port"
                prepend-icon="mdi-counter"
                persistent-placeholder
                :error-messages="connectionPortError"
            />
        </v-col>
      </v-row>

      <v-text-field
        v-if="showRemoteAccessUsername"
        v-model="remoteAccessUsername"
        label="Username"
        prepend-icon="mdi-account"
        :error="!!remoteAccessUsernameError"
        :error-messages="remoteAccessUsernameError"
      />

      <CredentialForm
        ref="credentialFormRef"
        :allow-existing="true"
        @changed="onCredentialFormChanged"
      />
    </template>
  </confirm-dialog>
</template>

<style scoped>

</style>