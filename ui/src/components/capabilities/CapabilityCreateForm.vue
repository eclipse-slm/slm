<script setup lang="ts">
import { computed, nextTick, ref, watch } from 'vue';
import { useField, useForm } from 'vee-validate';
import { useCapabilitiesStore } from '@/stores/capabilitiesStore';
import IconPickerDialog from '@/components/base/IconPickerDialog.vue';
import CapabilityActionCard from './CapabilityActionCard.vue';
import CapabilityImportDialog from './CapabilityImportDialog.vue';
import {
  allowedTypes,
  cloneActions,
  effectiveTypes,
  fallbackLogo,
  getActionTabColorProps,
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

const { errors, validate } = useForm();

const { value: name, errorMessage: nameError } = useField<string>('name', (v: string) => {
  if (!v || !v.trim()) return 'Name is required';
  return !isNameTaken(v, capabilitiesStore.capabilities) || 'A capability with this name already exists';
}, { initialValue: props.modelValue.formData.name });
const { value: logo } = useField<string>('logo', undefined, { initialValue: props.modelValue.formData.logo });
const { value: capabilityClass } = useField<CapabilityClass>('capabilityClass', undefined,
  { initialValue: props.modelValue.formData.capabilityClass });
const { value: deploymentTypes, errorMessage: deploymentTypesError } = useField<string[]>(
  'supportedDeploymentTypes',
  (v: string[]) => !isDeployTypeSelected.value || (v && v.length > 0)
    || 'Select at least one deployment type',
  { initialValue: props.modelValue.formData.supportedDeploymentTypes });
const { value: types, errorMessage: typesError } = useField<FormCapabilityType[]>('types',
  () => effectiveTypes(currentFormData()).length > 0 || 'Select at least one capability type',
  { initialValue: props.modelValue.formData.types });
const actions = ref<Partial<Record<FormActionType, ActionFormData>>>(
  cloneActions(props.modelValue.formData.actions));

const showIconPicker = ref(false);
const showImportDialog = ref(false);
const activeActionTab = ref<FormActionType | null>(null);

const isDeployment = computed(() => capabilityClass.value === 'DeploymentCapability');
const isDeployTypeSelected = computed(
  () => isDeployment.value && (types.value ?? []).includes('DEPLOY')
);
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
      actions.value = { ...actions.value, [t]: newActionFormData(t) };
    }
  }

  if (!needed.length) {
    activeActionTab.value = null;
    return;
  }

  if (!activeActionTab.value || !needed.includes(activeActionTab.value)) {
    activeActionTab.value = needed[0];
  }
}, { immediate: true });

watch(isDeployTypeSelected, (selected) => {
  if (!selected) {
    deploymentTypes.value = [];
  }
}, { immediate: true });

function setAction(t: FormActionType, value: ActionFormData) {
  actions.value = { ...actions.value, [t]: value };
}

function onSourceChanged(t: FormActionType) {
  if (t !== actionTypes.value[0]) return;
  actions.value = prefillUntouchedActions(actions.value, t);
}

async function onImported(f: CapabilityFormData) {
  showImportDialog.value = false;
  name.value = f.name;
  logo.value = f.logo;
  capabilityClass.value = f.capabilityClass;
  deploymentTypes.value = [...f.supportedDeploymentTypes];
  types.value = [...f.types];
  actions.value = cloneActions(f.actions);
  // Show errors of the imported values right away; wait until new action cards registered their fields
  await nextTick();
  await nextTick();
  await validate();
}

function openImport() {
  showImportDialog.value = true;
}

defineExpose({ openImport });

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
    <CapabilityImportDialog
      :show="showImportDialog"
      @imported="onImported"
      @canceled="showImportDialog = false"
    />

    <div class="text-subtitle-2 mb-2">Icon &amp; Capability Name</div>
    <v-text-field
      v-model="name"
      label="Name"
      required
      :prepend-icon="displayedLogo"
      :error-messages="nameError"
      @click:prepend="showIconPicker = true"
    />

    <IconPickerDialog
      :show="showIconPicker"
      :model-value="logo"
      @selected="onIconSelected"
      @canceled="showIconPicker = false"
    />

    <div class="mb-4">
      <div class="text-subtitle-2">Capability class</div>
      <v-radio-group v-model="capabilityClass" inline hide-details>
        <v-radio label="Deployment" value="DeploymentCapability" />
        <v-radio label="Base configuration" value="BaseConfigurationCapability" />
      </v-radio-group>
    </div>

    <div class="mb-4">
      <div class="text-subtitle-2">Capability types</div>
      <div class="d-flex align-center flex-wrap">
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
            class="mr-4"
            inline
          />
        </template>
        <v-checkbox
          v-else
          :model-value="true"
          label="SETUP"
          disabled
          hide-details
          density="compact"
          inline
        />
      </div>
      <div v-if="typesError" class="text-caption text-error">{{ typesError }}</div>
    </div>

    <div v-if="actionTypes.length" class="mt-2">
      <div class="text-subtitle-2 mb-2">Action types</div>

      <v-tabs
        v-model="activeActionTab"
        color="primary"
        density="compact"
        class="mb-2"
      >
        <v-tab
          v-for="t in actionTypes"
          :key="`tab-${t}`"
          :value="t"
          v-bind="getActionTabColorProps(t, errors)"
        >
          {{ t }}
        </v-tab>
      </v-tabs>

      <v-window v-model="activeActionTab">
        <v-window-item
          v-for="t in actionTypes"
          :key="`panel-${t}`"
          :value="t"
          eager
        >
          <CapabilityActionCard
            v-if="actions[t]"
            :action-type="t"
            :model-value="actions[t]!"
            @update:model-value="setAction(t, $event)"
            @source-changed="onSourceChanged(t)"
          />
        </v-window-item>
      </v-window>
    </div>

    <div v-if="isDeployTypeSelected" class="mb-2">
      <div class="text-subtitle-2 mb-2">Supported deployment types</div>
      <v-select
        v-model="deploymentTypes"
        multiple
        chips
        :items="deploymentTypeItems"
        :error-messages="deploymentTypesError"
      />
    </div>
  </form>
</template>
