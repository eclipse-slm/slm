<script setup lang="ts">
import { ref, watch, computed } from "vue";
import { useField } from "vee-validate";
import { useClipboard } from "@vueuse/core";
import { useToast } from "vue-toast-notification";

const props = defineProps({
  label: {
    type: String,
    default: ""
  },
  text: {
    type: [String, Number],
    default: ""
  },
  divider: {
    type: Boolean,
    default: true
  },
  editable: {
    type: Boolean,
    default: false
  },
  validationName: {
    type: String,
    default: ""
  },
  validationRules: {
    type: [String, Function, Object],
    default: null
  },
  copyable: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(["value-updated"]);

const isEditing = ref(false);
const { copy, isSupported } = useClipboard();
const $toast = useToast();
const validationEnabled = computed(() => !!props.validationRules);
const fieldName = computed(() => props.validationName || props.label || "value");

const editValue = ref(String(props.text ?? ""));
const displayValue = ref(String(props.text ?? ""));

const field = validationEnabled.value
  ? useField(fieldName.value, props.validationRules, {
      validateOnValueUpdate: true,
      initialValue: displayValue.value,
    })
  : null;

const fieldValue = field ? field.value : editValue;
const fieldErrorMessage = field ? field.errorMessage : ref("");
const isFieldValid = computed(() => (field ? field.meta.valid : true));

function resetFieldValue(value: string) {
  if (field) {
    field.resetField({ value, touched: false });
  } else {
    editValue.value = value;
  }
}

watch(() => props.text, (value) => {
  if (isEditing.value) {
    return;
  }
  const nextValue = String(value ?? "");
  if (nextValue === displayValue.value) {
    return;
  }
  displayValue.value = nextValue;
  resetFieldValue(nextValue);
});

function startEdit() {
  if (!props.editable) return;
  resetFieldValue(displayValue.value);
  isEditing.value = true;
}

async function confirmEdit() {
  if (!props.editable) return;
  if (field) {
    const result = await field.validate();
    if (!result.valid) {
      return;
    }
  }
  isEditing.value = false;
  const nextValue = String(fieldValue.value ?? "");
  displayValue.value = nextValue;
  emit("value-updated", nextValue);
}

function cancelEdit() {
  if (!props.editable) return;
  resetFieldValue(displayValue.value);
  isEditing.value = false;
}

function copyDisplayValue() {
  if (!props.copyable || !isSupported.value) return;
  if (!displayValue.value) return;
  copy(displayValue.value);
  $toast.info("Copied to clipboard");
}
</script>

<template>
  <div>
    <v-row>
      <v-col
        cols="2"
        class="ma-4"
        style="font-weight:bold"
      >
        <slot name="label">
          {{ props.label }}
        </slot>
      </v-col>
      <v-col class="ma-4">
        <slot name="content">
          <div class="editable-container">
            <div v-if="!isEditing">
              <span>{{ displayValue }}</span>
              <v-btn
                  v-if="props.copyable && isSupported"
                  class="copy-icon"
                  icon
                  size="x-small"
                  variant="text"
                  @click.stop="copyDisplayValue"
              >
                <v-icon icon="mdi-content-copy" />
              </v-btn>
              <v-btn
                v-if="props.editable"
                class="edit-icon"
                icon
                size="x-small"
                variant="text"
                @click.stop="startEdit"
              >
                <v-icon icon="mdi-pencil" />
              </v-btn>
            </div>
            <div v-else class="edit-row">
              <v-text-field
                v-model="fieldValue"
                density="compact"
                variant="outlined"
                class="edit-input"
                :error-messages="validationEnabled ? fieldErrorMessage : ''"
                :hide-details="validationEnabled ? 'auto' : true"
                @keydown.enter.prevent="confirmEdit"
                @keydown.esc.prevent="cancelEdit"
              />
              <v-btn
                icon
                size="x-small"
                variant="text"
                :disabled="validationEnabled && !isFieldValid"
                @click.stop="confirmEdit"
              >
                <v-icon icon="mdi-check" />
              </v-btn>
              <v-btn
                icon
                size="x-small"
                variant="text"
                @click.stop="cancelEdit"
              >
                <v-icon icon="mdi-close" />
              </v-btn>
            </div>
          </div>
        </slot>
      </v-col>
    </v-row>

    <v-divider v-if="props.divider" />
  </div>
</template>

<style scoped>
.v-divider {
  border-color: #000000;
}

.editable-container {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.edit-icon {
  opacity: 0;
  transition: opacity 0.15s ease-in-out;
}

.editable-container:hover .edit-icon {
  opacity: 1;
}

.copy-icon {
  opacity: 0;
  transition: opacity 0.15s ease-in-out;
}

.editable-container:hover .copy-icon {
  opacity: 1;
}

.edit-row {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.edit-input {
  min-width: 180px;
}
</style>