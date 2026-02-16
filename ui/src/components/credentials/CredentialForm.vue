<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import {useToast} from "vue-toast-notification";
import {CredentialDataType} from "@/api/platform-management/client";
import {debounce} from "chart.js/helpers";
import {CredentialFormData, KeyPairData, UsernamePasswordData} from "@/components/credentials/types";
import { defineExpose } from 'vue';
import { useForm, useField } from 'vee-validate';
import PlatformManagementClient from '@/api/platform-management/platform-management-client'
import type { CredentialReadDTO } from '@/api/platform-management/client'
import CredentialDisplay from '@/components/credentials/CredentialDisplay.vue'

const $toast = useToast();

const emit = defineEmits<{ (e: 'changed', formData: CredentialFormData ) : void }>();

// vee-validate form + fields
const { validate } = useForm();

const { value: credentialDataTypeVal, errorMessage: credentialDataTypeError } = useField<string | undefined>('credentialDataType', (v: any) => v ? true : 'Credential Type is required');
const { value: usernameVal, errorMessage: usernameError } = useField<string>('username', (v: any) => {
  if (credentialDataTypeVal.value === CredentialDataType.UsernamePassword) return v ? true : 'Username is required';
  return true;
});
const { value: passwordVal, errorMessage: passwordError } = useField<string>('password', (v: any) => {
  if (credentialDataTypeVal.value === CredentialDataType.UsernamePassword) return v ? true : 'Password is required';
  return true;
});
const { value: publicKeyVal, errorMessage: publicKeyError } = useField<string>('publicKey', (v: any) => {
  if (credentialDataTypeVal.value === CredentialDataType.KeyPair) return v ? true : 'Public key is required';
  return true;
});
const { value: privateKeyVal, errorMessage: privateKeyError } = useField<string>('privateKey', (v: any) => {
  if (credentialDataTypeVal.value === CredentialDataType.KeyPair) return v ? true : 'Private key is required';
  return true;
});
const { value: existingCredentialIdVal, errorMessage: existingCredentialIdError } = useField<string>('existingCredentialId', (v: any) => {
  if (credentialDataTypeVal.value === USE_EXISTING_VALUE) return v ? true : 'Existing credential is required';
  return true;
});

// keep credential options
const props = defineProps({
  allowExisting: { type: Boolean, default: false },
});
const USE_EXISTING_VALUE = 'USE_EXISTING';
const credentialTypeOptions = computed(() => {
  const options = [
    { title: 'Username / Password', value: CredentialDataType.UsernamePassword },
    { title: 'Key Pair', value: CredentialDataType.KeyPair },
  ];
  if (props.allowExisting) {
    options.push({ title: 'Use existing credential', value: USE_EXISTING_VALUE });
  }
  return options;
});

// keep file names and showPassword local refs
const showPassword = ref(false);
const publicKeyFileName = ref<string | null>(null);
const privateKeyFileName = ref<string | null>(null);

const existingCredentials = ref<CredentialReadDTO[]>([]);
const existingCredentialsLoading = ref(false);
const existingCredentialsError = ref<string | null>(null);

const existingCredentialOptions = computed(() => {
  return existingCredentials.value.map((credential) => {
    const type = credential.data?.credentialDataType;
    const username = (credential.data as any)?.username;
    const typeLabel = type === CredentialDataType.UsernamePassword ? 'Username / Password' : 'Key Pair';
    const detail = username ? ` - ${username}` : '';
    return {
      title: `${typeLabel}${detail} (${credential.id})`,
      value: credential.id,
    };
  });
});

const selectedExistingCredential = computed(() => {
  return existingCredentials.value.find((credential) => credential.id === existingCredentialIdVal.value) ?? null;
});

const emitDebounced = debounce(() => {
  // run vee-validate validation and then emit structured payload
  validate().then((validationResult) => {
      const formData = createFormData(validationResult.valid);
      emit('changed', formData);
  })
}, 300);

// Watcher fuer relevante Felder
watch([credentialDataTypeVal, usernameVal, passwordVal, publicKeyVal, privateKeyVal, existingCredentialIdVal], () => {
  emitDebounced();
});

watch(credentialDataTypeVal, (value) => {
  if (value === USE_EXISTING_VALUE && props.allowExisting) {
    loadExistingCredentials();
  }
  if (value !== USE_EXISTING_VALUE) {
    existingCredentialIdVal.value = '';
  }
});

function clearPublicKeyFile() {
  publicKeyFileName.value = null;
  publicKeyVal.value = '';
}
function clearPrivateKeyFile() {
  privateKeyFileName.value = null;
  privateKeyVal.value = '';
}

function onPublicKeyFileChange(file: File | null) {
  if (!file) {
    publicKeyFileName.value = null;
    publicKeyVal.value = '';
    return;
  }
  publicKeyFileName.value = file.name;
  const reader = new FileReader();
  reader.onload = () => {
    publicKeyVal.value = String(reader.result ?? '');
  };
  reader.onerror = () => {
    $toast.error("Error reading file");
  };
  reader.readAsText(file);
}
function onPrivateKeyFileChange(file: File | null) {
  if (!file) {
    privateKeyFileName.value = null;
    privateKeyVal.value = '';
    return;
  }
  privateKeyFileName.value = file.name;
  const reader = new FileReader();
  reader.onload = () => {
    privateKeyVal.value = String(reader.result ?? '');
  };
  reader.onerror = () => {
    $toast.error("Error reading file");
  };
  reader.readAsText(file);
}

// Vuetify v-file-input can emit File | File[] | null; provide wrappers that accept that signature
function onPublicKeyFileChangeWrapper(files: File | File[] | null) {
  let file: File | null = files && Array.isArray(files) ? files[0] ?? null : (files as File | null);
  onPublicKeyFileChange(file);
}
function onPrivateKeyFileChangeWrapper(files: File | File[] | null) {
  let file: File | null = files && Array.isArray(files) ? files[0] ?? null : (files as File | null);
  onPrivateKeyFileChange(file);
}

async function loadExistingCredentials() {
  if (existingCredentialsLoading.value) return;
  existingCredentialsLoading.value = true;
  existingCredentialsError.value = null;
  try {
    const response = await PlatformManagementClient.credentialsApi.getCredentialsOfUser();
    existingCredentials.value = response.data ?? [];
  } catch (err: any) {
    existingCredentialsError.value = err?.message ?? 'Error loading credentials';
  } finally {
    existingCredentialsLoading.value = false;
  }
}

function clearForm() {
  credentialDataTypeVal.value = undefined;
  usernameVal.value = '';
  passwordVal.value = '';
  publicKeyVal.value = '';
  privateKeyVal.value = '';
  publicKeyFileName.value = null;
  privateKeyFileName.value = null;
  existingCredentialIdVal.value = '';
  existingCredentialsError.value = null;
}

// expose clear and getter to parent
defineExpose({ clearForm, createFormData });

function createFormData(isFormValid: boolean): CredentialFormData {
  let credentialFormData = {
    isFormValid: isFormValid,
  } as CredentialFormData;

  if (credentialDataTypeVal.value === USE_EXISTING_VALUE) {
    credentialFormData.useExisting = true;
    credentialFormData.existingCredentialId = existingCredentialIdVal.value;
    credentialFormData.existingCredentialDataType = selectedExistingCredential.value?.data?.credentialDataType;
    return credentialFormData;
  }

  if (credentialDataTypeVal.value === CredentialDataType.UsernamePassword) {
    credentialFormData.data = {
      credentialDataType: credentialDataTypeVal.value,
      username: usernameVal.value,
      password: passwordVal.value,
    } as UsernamePasswordData
  } else if (credentialDataTypeVal.value === CredentialDataType.KeyPair) {
    credentialFormData.data = {
      credentialDataType: credentialDataTypeVal.value,
      publicKey: publicKeyVal.value,
      privateKey: privateKeyVal.value,
    } as KeyPairData
  }

  return credentialFormData;
}
</script>

<template>
  <div>
    <!-- STEP 1: Credential Type -->
    <v-select
      v-model="credentialDataTypeVal"
      :items="credentialTypeOptions"
      item-title="title"
      item-value="value"
      label="Credential Type"
      placeholder="Select type of credential"
      density="comfortable"
      :error="!!credentialDataTypeError"
      :error-messages="credentialDataTypeError"
    />

    <div v-if="credentialDataTypeVal === USE_EXISTING_VALUE">
      <v-alert v-if="existingCredentialsError" type="error" variant="tonal" class="mb-2">
        {{ existingCredentialsError }}
      </v-alert>

      <v-select
        v-model="existingCredentialIdVal"
        :items="existingCredentialOptions"
        item-title="title"
        item-value="value"
        label="Existing Credential"
        placeholder="Select existing credential"
        density="comfortable"
        :loading="existingCredentialsLoading"
        :error="!!existingCredentialIdError"
        :error-messages="existingCredentialIdError"
      />

      <div v-if="selectedExistingCredential" class="mt-2">
        <CredentialDisplay :credential="selectedExistingCredential" />
      </div>
    </div>

    <!-- STEP 2: Fields based on selected Credential Type -->
    <!-- STEP 2: Username/Password -->
    <div v-else-if="credentialDataTypeVal === CredentialDataType.UsernamePassword">
      <v-text-field
          v-model="usernameVal"
          label="Username"
          :error="!!usernameError"
          :error-messages="usernameError"
      />
      <v-text-field
          v-model="passwordVal"
          label="Password"
          :type="showPassword ? 'text' : 'password'"
          :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
          @click:append-inner="showPassword = !showPassword"
          :error="!!passwordError"
          :error-messages="passwordError"
      />
    </div>

    <!-- STEP 2: Key Pair -->
    <div v-else-if="credentialDataTypeVal === CredentialDataType.KeyPair">
      <div class="mt-2 mb-2">
        <div class="text-subtitle-2">Public Key</div>
        <div class="text-caption">Enter public key or upload key file</div>
      </div>
      <div>
        <v-file-input
          id="public-key-file-input"
          accept=".pem,.key,text/plain"
          density="compact"
          clearable
          @click:clear="clearPublicKeyFile"
          @update:modelValue="onPublicKeyFileChangeWrapper"
        />
        <v-textarea
            v-model="publicKeyVal"
            auto-grow
            rows="1"
            max-rows="6"
            :error="!!publicKeyError"
            :error-messages="publicKeyError"
        />
      </div>

      <div class="mt-2 mb-2">
          <div class="text-subtitle-2">Private Key</div>
          <div class="text-caption">Enter private key or upload key file</div>
      </div>
      <div>
        <v-file-input
          id="private-key-file-input"
          accept=".pem,.key,text/plain"
          density="compact"
          no-resize
          @click:clear="clearPrivateKeyFile"
          @update:modelValue="onPrivateKeyFileChangeWrapper"
        />
        <v-textarea
            v-model="privateKeyVal"
            rows="6"
            no-resize
            :auto-grow="false"
            :error="!!privateKeyError"
            :error-messages="privateKeyError"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
