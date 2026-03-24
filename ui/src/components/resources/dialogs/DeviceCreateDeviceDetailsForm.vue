<script setup lang="ts">
import { watch, computed } from 'vue';
import { useForm, useField } from 'vee-validate';
import type { DeviceDetailsForm } from './DeviceDialogCreateTypes';
import { useResourceDevicesStore } from '@/stores/resourceDevicesStore';

const emit = defineEmits<{ (e: 'update:modelValue', value: DeviceDetailsForm): void; }>();
const props = defineProps<{ modelValue: DeviceDetailsForm }>();

const resourceDevicesStore = useResourceDevicesStore();
const locations = computed(() => resourceDevicesStore.locations);

// Validation rules
const hostnameRule = (v: string) => /^[A-Za-z0-9_-]+$/.test(v) || 'Invalid hostname';
const ipRule = (v: string) => /^\d{1,3}(?:\.\d{1,3}){3}$/.test(v) || 'Invalid IP';

// vee-validate form
const { validate, meta } = useForm({
  initialValues: props.modelValue?.formData || {
    hostname: '',
    ip: '',
    locationId: '',
    assetId: '',
    manufacturerName: '',
    product: '',
  },
});

const { value: hostname, errorMessage: hostnameError } = useField<string>('hostname', (v: string) => v ? hostnameRule(v) : 'Hostname is required');
const { value: ip, errorMessage: ipError } = useField<string>('ip', (v: string) => v ? ipRule(v) : 'IP is required');
const { value: locationId } = useField<string>('locationId');
const { value: assetId } = useField<string>('assetId');
const { value: manufacturerName } = useField<string>('manufacturerName');
const { value: product } = useField<string>('product');

// Sync incoming modelValue to fields
watch(() => props.modelValue, (val) => {
  if (val && val.formData) {
    hostname.value = val.formData.hostname || '';
    ip.value = val.formData.ip || '';
    locationId.value = val.formData.locationId || '';
    assetId.value = val.formData.assetId || '';
    manufacturerName.value = val.formData.manufacturerName || '';
    product.value = val.formData.product || '';
  }
}, { immediate: true });

// Emit changes to parent
watch([hostname, ip, locationId, assetId, manufacturerName, product], async () => {
  const form: DeviceDetailsForm = {
    isFormValid: false,
    formData: {
      hostname: hostname.value,
      ip: ip.value,
      locationId: locationId.value,
      assetId: assetId.value,
      manufacturerName: manufacturerName.value,
      product: product.value,
    }
  };
  const result = await validate();
  form.isFormValid = result.valid;
  emit('update:modelValue', form);
});
</script>

<template>
  <form @submit.prevent>
    <v-text-field
      v-model="hostname"
      label="Hostname"
      required
      prepend-icon="mdi-dns"
      :error-messages="hostnameError"
    />
    <v-text-field
      v-model="ip"
      label="IP"
      required
      prepend-icon="mdi-ip"
      :error-messages="ipError"
    />
    <v-select
      v-if="locations.length > 0"
      v-model="locationId"
      label="Resource Location"
      prepend-icon="mdi-map-marker"
      :items="locations"
      item-title="name"
      item-value="id"
      clearable
    />
    <v-text-field
      v-model="assetId"
      label="Asset ID"
      prepend-icon="mdi-barcode"
    />
    <v-text-field
      v-model="manufacturerName"
      label="Manufacturer Name"
      prepend-icon="mdi-factory"
    />
    <v-text-field
      v-model="product"
      label="Product"
      prepend-icon="mdi-cube"
    />
  </form>
</template>
