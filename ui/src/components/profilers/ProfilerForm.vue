<script setup lang="ts">
import { nextTick, watch } from 'vue';
import { useField, useForm } from 'vee-validate';
import type { Profiler } from '@/api/resource-management/client';
import { useProfilersStore } from '@/stores/profilersStore';
import { credentialsError, isNameTaken } from './profilerForm';
import type { ProfilerForm, ProfilerFormData } from './profilerForm';

const props = defineProps<{ modelValue: ProfilerForm; edit: boolean; profiler?: Profiler | null }>();
const emit = defineEmits<{ (e: 'update:modelValue', value: ProfilerForm): void }>();

const profilersStore = useProfilersStore();

const credentialsHint = 'Only needed if the repository, branch or playbook changes and the repository is private.';
const parameterNames = (props.profiler?.action?.parameter ?? []).map((p) => p.variable);

const { validate } = useForm();

function required(label: string) {
  return (v: string) => !!v && v.trim() !== '' || `${label} is required`;
}

const { value: name, errorMessage: nameError } = useField<string>('name', (v: string) => {
  if (!v || !v.trim()) return 'Name is required';
  return !isNameTaken(v, profilersStore.profilers, props.profiler?.id) || 'A profiler with this name already exists';
}, { initialValue: props.modelValue.formData.name, validateOnMount: props.edit });
const { value: awxRepo, errorMessage: repoError } = useField<string>('awxRepo', required('Repository'),
  { initialValue: props.modelValue.formData.awxRepo });
const { value: awxBranch, errorMessage: branchError } = useField<string>('awxBranch', required('Branch'),
  { initialValue: props.modelValue.formData.awxBranch });
const { value: playbook, errorMessage: playbookError } = useField<string>('playbook', required('Playbook'),
  { initialValue: props.modelValue.formData.playbook });
const { value: username, errorMessage: usernameError, validate: validateUsername } = useField<string>('username',
  () => credentialsError(currentFormData()),
  { initialValue: props.modelValue.formData.username });
const { value: password, errorMessage: passwordError, validate: validatePassword } = useField<string>('password',
  () => credentialsError(currentFormData()),
  { initialValue: props.modelValue.formData.password });

function currentFormData(): ProfilerFormData {
  return {
    name: name.value ?? '',
    awxRepo: awxRepo.value ?? '',
    awxBranch: awxBranch.value ?? '',
    playbook: playbook.value ?? '',
    username: username.value ?? '',
    password: password.value ?? '',
  };
}

// Each credential's error depends on the other one
watch([username, password], () => {
  validateUsername();
  validatePassword();
});

// Sync incoming modelValue (e.g. dialog reset) to fields
watch(() => props.modelValue, (val) => {
  if (JSON.stringify(val.formData) === JSON.stringify(currentFormData())) return;
  const f = val.formData;
  name.value = f.name;
  awxRepo.value = f.awxRepo;
  awxBranch.value = f.awxBranch;
  playbook.value = f.playbook;
  username.value = f.username;
  password.value = f.password;
});

// Emit changes to parent
let validationRun = 0;
watch([name, awxRepo, awxBranch, playbook, username, password], async () => {
  const run = ++validationRun;
  await nextTick();
  const result = await validate({ mode: 'silent' }); // silent: do not flag untouched fields as errors
  if (run !== validationRun) return; // a newer change superseded this run
  emit('update:modelValue', { isFormValid: result.valid, formData: currentFormData() });
}, { immediate: true });
</script>

<template>
  <form @submit.prevent>
    <v-text-field
      v-model="name"
      label="Name"
      required
      :error-messages="nameError"
    />
    <v-text-field
      v-model="awxRepo"
      label="Repository"
      required
      :error-messages="repoError"
    />
    <v-text-field
      v-model="awxBranch"
      label="Branch"
      required
      :error-messages="branchError"
    />
    <v-text-field
      v-model="playbook"
      label="Playbook"
      required
      :error-messages="playbookError"
    />
    <v-text-field
      v-model="username"
      label="Username (optional)"
      autocomplete="off"
      :hint="edit ? credentialsHint : undefined"
      :persistent-hint="edit"
      :error-messages="usernameError"
    />
    <v-text-field
      v-model="password"
      label="Password (optional)"
      type="password"
      autocomplete="new-password"
      :hint="edit ? credentialsHint : undefined"
      :persistent-hint="edit"
      :error-messages="passwordError"
    />

    <div v-if="parameterNames.length" class="mt-2">
      <div class="text-subtitle-2 mb-1">Parameters</div>
      <v-chip
        v-for="variable in parameterNames"
        :key="variable"
        class="mr-1"
        size="small"
      >
        {{ variable }}
      </v-chip>
    </div>
  </form>
</template>
