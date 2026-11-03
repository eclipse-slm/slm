<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue';
import { useField, useForm } from 'vee-validate';
import { useCapabilitiesStore } from '@/stores/capabilitiesStore';
import IconPickerDialog from '@/components/base/IconPickerDialog.vue';
import CapabilityActionCard from './CapabilityActionCard.vue';
import {
  allowedTypes,
  cloneActions,
  effectiveTypes,
  fallbackLogo,
  isNameTaken,
  newActionFormData,
  prefillUntouchedActions,
  requiredActions,
} from './capabilityForm';
import type {
  ActionFormData,
  CapabilityClass,
  CapabilityForm,
  CapabilityFormData,
  FormActionType,
  FormCapabilityType,
} from './capabilityForm';

const props = defineProps<{ modelValue: CapabilityForm }>();
const emit = defineEmits<{ (e: 'update:modelValue', value: CapabilityForm): void }>();

const capabilitiesStore = useCapabilitiesStore();

const deploymentTypeItems = [
  { title: 'Docker container', value: 'DOCKER_CONTAINER' },
  { title: 'Docker compose', value: 'DOCKER_COMPOSE' },
  { title: 'Kubernetes', value: 'KUBERNETES' },
  { title: 'CODESYS', value: 'CODESYS' },
];

const { validate } = useForm();

const { value: name, errorMessage: nameError } = useField<string>('name', (v: string) => {
  if (!v || !v.trim()) return 'Name is required';
  return !isNameTaken(v, capabilitiesStore.capabilities) || 'A capability with this name already exists';
}, { initialValue: props.modelValue.formData.name });
const { value: logo } = useField<string>('logo', undefined, { initialValue: props.modelValue.formData.logo });
const { value: capabilityClass } = useField<CapabilityClass>('capabilityClass', undefined,
  { initialValue: props.modelValue.formData.capabilityClass });
const { value: deploymentTypes, errorMessage: deploymentTypesError } = useField<string[]>(
  'supportedDeploymentTypes',
  (v: string[]) => capabilityClass.value !== 'DeploymentCapability' || (v && v.length > 0)
    || 'Select at least one deployment type',
  { initialValue: props.modelValue.formData.supportedDeploymentTypes });
const { value: types, errorMessage: typesError } = useField<FormCapabilityType[]>('types',
  () => effectiveTypes(currentFormData()).length > 0 || 'Select at least one capability type',
  { initialValue: props.modelValue.formData.types });
const actions = ref<Partial<Record<FormActionType, ActionFormData>>>(
  cloneActions(props.modelValue.formData.actions));

const showIconPicker = ref(false);

const isDeployment = computed(() => capabilityClass.value === 'DeploymentCapability');
const typeChoices = computed(() => allowedTypes(capabilityClass.value));
const displayedLogo = computed(() => logo.value || fallbackLogo(name.value ?? ''));
const actionTypes = computed(() => requiredActions(currentFormData()));

function currentFormData(): CapabilityFormData {
  return {
    name: name.value ?? '',
    logo: logo.value ?? '',
    capabilityClass: capabilityClass.value,
    supportedDeploymentTypes: [...(deploymentTypes.value ?? [])],
    types: [...(types.value ?? [])],
    actions: actions.value,
  };
}

// Sync incoming modelValue (e.g. dialog reset) to fields
watch(() => props.modelValue, (val) => {
  if (JSON.stringify(val.formData) === JSON.stringify(currentFormData())) return;
  const f = val.formData;
  name.value = f.name;
  logo.value = f.logo;
  capabilityClass.value = f.capabilityClass;
  deploymentTypes.value = [...f.supportedDeploymentTypes];
  types.value = [...f.types];
  actions.value = cloneActions(f.actions);
});

// Create missing action entries when their type appears
watch(actionTypes, (needed) => {
  for (const t of needed) {
    if (!actions.value[t]) {
      actions.value = { ...actions.value, [t]: newActionFormData() };
    }
  }
}, { immediate: true });

function setAction(t: FormActionType, value: ActionFormData) {
  actions.value = { ...actions.value, [t]: value };
}

function onSourceChanged(t: FormActionType) {
  if (t !== actionTypes.value[0]) return;
  actions.value = prefillUntouchedActions(actions.value, t);
}

function onIconSelected(icon: string) {
  logo.value = icon;
  showIconPicker.value = false;
}

// Emit changes to parent
let validationRun = 0;
watch([name, logo, capabilityClass, deploymentTypes, types, actions], async () => {
  const run = ++validationRun;
  await nextTick(); // let newly mounted action cards register their fields
  const result = await validate({ mode: 'silent' }); // silent: do not flag untouched fields as errors
  if (run !== validationRun) return; // a newer change superseded this run
  emit('update:modelValue', { isFormValid: result.valid, formData: currentFormData() });
}, { deep: true, immediate: true });
</script>

<template>
  <form @submit.prevent>
    <v-text-field
      v-model="name"
      label="Name"
      required
      prepend-icon="mdi-puzzle"
      :error-messages="nameError"
    />

    <div class="d-flex align-center mb-4">
      <v-btn variant="outlined" @click="showIconPicker = true">
        <v-icon start>{{ displayedLogo }}</v-icon>
        {{ displayedLogo }}
      </v-btn>
      <v-btn
        v-if="logo"
        icon="mdi-close"
        variant="text"
        size="small"
        class="ml-2"
        aria-label="Clear logo"
        @click="logo = ''"
      />
      <span v-else class="text-caption ml-3">Default: first letter of name</span>
    </div>
    <IconPickerDialog
      :show="showIconPicker"
      :model-value="logo"
      @selected="onIconSelected"
      @canceled="showIconPicker = false"
    />

    <v-radio-group v-model="capabilityClass" inline label="Class">
      <v-radio label="Deployment" value="DeploymentCapability" />
      <v-radio label="Base configuration" value="BaseConfigurationCapability" />
    </v-radio-group>

    <v-select
      v-if="isDeployment"
      v-model="deploymentTypes"
      label="Supported deployment types"
      multiple
      chips
      :items="deploymentTypeItems"
      :error-messages="deploymentTypesError"
    />

    <div class="mb-4">
      <div class="text-subtitle-2">Capability types</div>
      <template v-if="isDeployment">
        <v-checkbox
          v-for="t in typeChoices"
          :key="t"
          v-model="types"
          :label="t"
          :value="t"
          multiple
          hide-details
          density="compact"
        />
      </template>
      <v-checkbox
        v-else
        :model-value="true"
        label="SETUP"
        disabled
        hide-details
        density="compact"
      />
      <div v-if="typesError" class="text-caption text-error">{{ typesError }}</div>
    </div>

    <template v-for="t in actionTypes" :key="t">
      <CapabilityActionCard
        v-if="actions[t]"
        :action-type="t"
        :model-value="actions[t]!"
        @update:model-value="setAction(t, $event)"
        @source-changed="onSourceChanged(t)"
      />
    </template>
  </form>
</template>
