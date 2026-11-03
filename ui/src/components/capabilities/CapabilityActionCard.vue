<script setup lang="ts">
import { watch } from 'vue';
import { useField } from 'vee-validate';
import type { ActionFormData, FormActionType } from './capabilityForm';

const props = defineProps<{ actionType: FormActionType; modelValue: ActionFormData }>();
const emit = defineEmits<{
  (e: 'update:modelValue', value: ActionFormData): void;
  (e: 'source-changed'): void;
}>();

const repoRule = (v: string) => {
  if (!v) return 'AWX repository is required';
  return /^(https?:\/\/|git@)/.test(v) || 'Must start with http://, https:// or git@';
};
const requiredRule = (label: string) => (v: string) => !!v || `${label} is required`;

const prefix = props.actionType;
const { value: awxRepo, errorMessage: repoError } = useField<string>(`${prefix}.awxRepo`, repoRule, {
  initialValue: props.modelValue.awxRepo,
});
const { value: awxBranch, errorMessage: branchError } = useField<string>(
  `${prefix}.awxBranch`, requiredRule('Branch'), { initialValue: props.modelValue.awxBranch });
const { value: playbook, errorMessage: playbookError } = useField<string>(
  `${prefix}.playbook`, requiredRule('Playbook'), { initialValue: props.modelValue.playbook });

watch(() => props.modelValue, (val) => {
  awxRepo.value = val.awxRepo;
  awxBranch.value = val.awxBranch;
  playbook.value = val.playbook;
});

watch([awxRepo, awxBranch, playbook], () => {
  const m = props.modelValue;
  if (m.awxRepo === awxRepo.value && m.awxBranch === awxBranch.value && m.playbook === playbook.value) {
    return;
  }
  emit('update:modelValue', {
    awxRepo: awxRepo.value ?? '',
    awxBranch: awxBranch.value ?? '',
    playbook: playbook.value ?? '',
  });
});
</script>

<template>
  <v-card variant="outlined" class="mt-4 mb-4">
    <v-card-text>
      <v-text-field
        v-model="awxRepo"
        label="AWX repository"
        required
        class="mb-2"
        hide-details="auto"
        :error-messages="repoError"
        @blur="emit('source-changed')"
      />
      <v-text-field
        v-model="awxBranch"
        label="Branch"
        required
        class="mb-2"
        hide-details="auto"
        :error-messages="branchError"
        @blur="emit('source-changed')"
      />
      <v-text-field
        v-model="playbook"
        label="Playbook"
        required
        hide-details="auto"
        :error-messages="playbookError"
      />
    </v-card-text>
  </v-card>
</template>
