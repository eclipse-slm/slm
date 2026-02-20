<script setup lang="ts">
import {computed, ref, watch} from "vue";
import {useField, useForm} from "vee-validate";
import CredentialForm from '@/components/credentials/CredentialForm.vue';
import {CredentialFormData} from '@/components/credentials/CredentialTypes';
import {CredentialDataType} from "@/api/platform-management/client";
import {RemoteAccessFormData} from "@/components/resources/remoteaccess/RemoteAccessTypes";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import {storeToRefs} from "pinia";
import {debounce} from "chart.js/helpers";

const emit = defineEmits<{ (e: 'update:modelValue', value: RemoteAccessFormData): void }>();
const props = defineProps<{ modelValue: RemoteAccessFormData }>();

// Stores
const resourceDevicesStores = useResourceDevicesStore();
const { resourceConnectionTypes } = storeToRefs(resourceDevicesStores);

// Neues v-model für CredentialForm
const credentialFormData = ref<CredentialFormData>({ isFormValid: false, formData: {} });
const remoteAccessUsername = ref(props.modelValue?.formData?.remoteAccessUsername || '');

const showRemoteAccessUsername = computed(() => {
  const formData = credentialFormData.value?.formData || {};
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

// vee-validate form + fields
const { validate } = useForm({
  initialValues: props.modelValue?.formData || {
    connectionTypeName: resourceConnectionTypes.value[0]?.name || '',
    connectionPort: resourceConnectionTypes.value[0]?.defaultPort || '',
    remoteAccessUsername: '',
    credentialFormData: undefined
  }
});
const { value: connectionTypeField, errorMessage: connectionTypeError } = useField<string>('connectionTypeName', (v: any) => v ? true : 'Connection Type is required');
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

// Sync incoming modelValue to fields
watch(() => props.modelValue, (val) => {
  if (val && val.formData) {
    credentialFormData.value = val.formData.credentialFormData || { isFormValid: false, formData: {} };
    remoteAccessUsername.value = val.formData.remoteAccessUsername || '';
    connectionTypeField.value = val.formData.connectionTypeName || resourceConnectionTypes.value[0]?.name || '';
    connectionPortField.value = val.formData.connectionPort || resourceConnectionTypes.value[0]?.defaultPort || '';
  }
}, { immediate: true });

const credentialFormValid = computed(() => credentialFormData.value?.isFormValid ?? false);

const emitDebounced = debounce(() => {
  validate().then((validationResult) => {
    const formValid = validationResult.valid && credentialFormValid.value;
    const formData = createFormData(formValid);
    emit('update:modelValue', formData);
  });
}, 300);

watch([credentialFormData, remoteAccessUsername, connectionTypeField, connectionPortField], () => {
  emitDebounced();
});

function createFormData(isFormValid: boolean): RemoteAccessFormData {
  return {
    isFormValid: isFormValid,
    formData: {
      credentialFormData: credentialFormData.value,
      remoteAccessUsername: remoteAccessUsername.value,
      connectionTypeName: connectionTypeField.value,
      connectionPort: connectionPortField.value
    }
  } as RemoteAccessFormData;
}
</script>

<template>
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
      v-model="credentialFormData"
      :allow-existing="true"
  />
</template>

<style scoped>
</style>
