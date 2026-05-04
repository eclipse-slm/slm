<template>
  <v-dialog :model-value="modelValue" max-width="460" persistent @update:model-value="onModelUpdate">
    <v-card>
      <v-card-title class="text-h6">{{ title }}</v-card-title>
      <v-card-text>
        <slot>
          {{ text }}
        </slot>
      </v-card-text>
      <v-card-actions>
        <v-btn variant="text" :disabled="loading" @click="emit('cancel')">
          {{ cancelText }}
        </v-btn>
        <v-spacer />
        <v-btn :color="confirmColor" :loading="loading" :disabled="loading || confirmDisabled" @click="emit('confirm')">
          {{ confirmText }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void;
  (event: 'confirm'): void;
  (event: 'cancel'): void;
}>();

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    title: string;
    text: string;
    confirmText?: string;
    cancelText?: string;
    confirmColor?: string;
    confirmDisabled?: boolean;
    loading?: boolean;
  }>(),
  {
    confirmText: 'Confirm',
    cancelText: 'Cancel',
    confirmColor: 'primary',
    confirmDisabled: false,
    loading: false
  }
);

function onModelUpdate(value: boolean) {
  emit('update:modelValue', value);
  if (!value) {
    emit('cancel');
  }
}
</script>

