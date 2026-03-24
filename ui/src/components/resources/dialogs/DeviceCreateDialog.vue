<script setup lang="ts">
import { ref, onMounted, toRef } from 'vue';
import logRequestError from '@/api/restApiHelper';
import { useUserStore } from '@/stores/userStore';
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";
import RemoteAccessForm from "@/components/resources/remoteaccess/RemoteAccessForm.vue";
import {RemoteAccessFormData} from "@/components/resources/remoteaccess/RemoteAccessTypes";
import { remoteAccessHandler } from "@/components/resources/remoteaccess/RemoteAccessHandler";
import DeviceCreateDeviceDetailsForm from "@/components/resources/dialogs/DeviceCreateDeviceDetailsForm";
import { DeviceDetailsForm } from "@/components/resources/dialogs/DeviceDialogCreateTypes";
import {useToast} from "vue-toast-notification";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  }
});
const emit = defineEmits(['canceled', 'confirmed']);

const $toast = useToast();

// Dialog visibility
const active = toRef(props, 'show');

// Wizard Config
const deviceDetailsLoading = ref(false);
const remoteAccessLoading = ref(false);

const stepperItems = [
  {
    title: 'Device Details',
    value: 1,
    backButtonText: "Cancel",
    nextButtonText: "Create device",
    confirmAction: createResource,
    isValid: () => deviceDetailsForm.value.isFormValid,
    isLoading: () => deviceDetailsLoading.value
  },
  {
    title: 'Remote Access',
    value: 2,
    backButtonText: "Skip",
    nextButtonText: "Add remote access",
    confirmAction: addRemoteAccess,
    isValid: () => remoteAccessFormData.value.isFormValid,
    isLoading: () => remoteAccessLoading.value
  }
];
const currentStepNumber = ref(1);
const currentStepperItem = ref(stepperItems[currentStepNumber.value-1])

// Stores
const userStore = useUserStore();

// Refs
const deviceDetailsForm = ref<DeviceDetailsForm>({isFormValid: false, formData: {} });
const createdResourceId = ref<string | null>(null);

// Lifecycle
onMounted(() => {
  currentStepperItem.value = stepperItems[0]
});

// Methods
function clearDialog() {
  deviceDetailsForm.value = {isFormValid: false, formData: {}};
  remoteAccessFormData.value = { isFormValid: false, formData: {} };
  createdResourceId.value = null;
  currentStepNumber.value = 1;
  currentStepperItem.value = stepperItems[currentStepNumber.value-1];
}

function confirmDialog() {
  clearDialog();
  emit('confirmed');
}

function closeDialog() {
  clearDialog();
  emit('canceled');
}

const remoteAccessFormData = ref<RemoteAccessFormData>({ isFormValid: false, formData: {} });

function moveToNextStep() {
  if (currentStepNumber.value < stepperItems.length) {
    currentStepNumber.value += 1;
    currentStepperItem.value = stepperItems[currentStepNumber.value - 1]
  }
}

async function createResource() {
  if (!deviceDetailsForm.value.isFormValid) {
    $toast.error('Please fill in all required fields correctly');
    return;
  }
  deviceDetailsLoading.value = true;
  try {
    const response = await ResourceManagementClient.resourcesApi.addExistingResource({
      resourceHostname: deviceDetailsForm.value.formData.hostname,
      resourceIp: deviceDetailsForm.value.formData.ip,
      digitalNameplateV3: {
        uriOfTheProduct: deviceDetailsForm.value.formData.assetId ?? '',
        manufacturerName: deviceDetailsForm.value.formData.manufacturerName ?? '',
        manufacturerProductDesignation: deviceDetailsForm.value.formData.product ?? '',
        addressInformation: '',
      },
      fullPathOwnerGroupId: userStore.fullPathUserGroupId
    });
    if (response.status === 201) {
      createdResourceId.value = response.data
      $toast.info("Device created successfully");
      moveToNextStep();
    }
    else {
      $toast.error('Failed to create device');
      console.error('Unexpected response while creating resource:', response);
    }
  } catch (e) {
    logRequestError(e);
  } finally {
    deviceDetailsLoading.value = false;
  }
}

async function addRemoteAccess() {
  if (!remoteAccessFormData.value.isFormValid) {
    $toast.error('Please fill in all required fields correctly');
    return;
  }
  if (!createdResourceId.value) {
    console.log("Created resource ID is not set, cannot add remote access");
    return;
  }
  remoteAccessLoading.value = true;
  try {
    await remoteAccessHandler().addRemoteAccess(
      createdResourceId.value,
      userStore.fullPathUserGroupId,
      remoteAccessFormData.value
    );
    $toast.info("Remote access added successfully");
    confirmDialog()
  } catch (e) {
    logRequestError(e);
  } finally {
    remoteAccessLoading.value = false;
  }
}

function onNextButtonClicked() {
  currentStepperItem.value = stepperItems[currentStepNumber.value-1]
  currentStepNumber.value = currentStepperItem.value.value;

  const action = currentStepperItem.value.confirmAction;
  if (typeof action === 'function') {
    action();
  }
}
</script>

<template>
  <ConfirmDialog
    :show="active"
    :title="'Add device'"
    :width="'800px'"
    :cancel-button-label="currentStepperItem.backButtonText"
    :confirm-button-label="currentStepperItem.nextButtonText"
    :confirm-button-disabled="!currentStepperItem.isValid()"
    :confirm-loading="currentStepperItem.isLoading()"
    @canceled="closeDialog"
    @confirmed="onNextButtonClicked()"
  >
    <template #content>
        <v-stepper
            flat
        >
          <template v-slot:default>
            <v-stepper-header elevation="0">
              <template v-for="stepperItem in stepperItems" :key="`step-${stepperItem.value}`">
                <v-stepper-item
                    :complete="currentStepNumber > stepperItem.value"
                    :step="`Step {{ n }}`"
                    :value="stepperItem.value"
                >
                  {{ stepperItem.title }}
                </v-stepper-item>

                <v-divider
                    v-if="stepperItem.value !== stepperItems.length"
                    :key="stepperItem.value"
                ></v-divider>
              </template>
            </v-stepper-header>

          <v-stepper-window v-model="currentStepNumber" elevation="0">
            <v-stepper-window-item :value="1">
              <DeviceCreateDeviceDetailsForm
                v-model="deviceDetailsForm"
              />
            </v-stepper-window-item>

            <v-stepper-window-item :value="2">
              <RemoteAccessForm
                  v-model="remoteAccessFormData"
              />
            </v-stepper-window-item>
          </v-stepper-window>
          </template>
        </v-stepper>
    </template>
  </ConfirmDialog>
</template>

<style scoped>
.v-divider {
  border-color: #000000;
}
.v-stepper-header {
  box-shadow: 0 1px 0 0 rgba(var(--v-theme-primary), 0.3) !important;
}
</style>
