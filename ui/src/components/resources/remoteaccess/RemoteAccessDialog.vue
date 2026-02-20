<script setup lang="ts">
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";
import {ref, computed} from "vue";
import logRequestError from "@/api/restApiHelper";
import {useToast} from "vue-toast-notification";
import {useUserStore} from "@/stores/userStore";
import RemoteAccessForm from './RemoteAccessForm.vue';
import {RemoteAccessFormData} from "@/components/resources/remoteaccess/RemoteAccessTypes";
import { remoteAccessHandler } from '@/components/resources/remoteaccess/RemoteAccessHandler';

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  resourceId: {
    type: String,
    required: true
  }
});

const emit = defineEmits(['canceled', 'confirmed']);

const $toast = useToast();
const active = computed(() => props.show);

// Stores
const userStore = useUserStore();

// Form Data
const remoteAccessFormData = ref<RemoteAccessFormData>({ isFormValid: false, formData: {} });

// Handler
const { addRemoteAccess } = remoteAccessHandler();

// Event Handling
const confirmLoading = ref(false);
const onConfirmClicked = async () => {
  if (confirmLoading.value) {
    return;
  }
  confirmLoading.value = true;
  try {
    try {
      await addRemoteAccess(props.resourceId, userStore.fullPathUserGroupId, remoteAccessFormData.value);
      $toast.info("Remote access successfully added");
      emit('confirmed');
    } catch (e: any) {
      $toast.error(e.message || 'Error creating remote access');
    }
  } catch (e) {
    $toast.error("Error adding remote access");
    logRequestError(e);
  } finally {
    confirmLoading.value = false;
  }
};
</script>

<template>
  <confirm-dialog
      :show="active"
      title="Add remote access"
      cancel-button-label="Cancel"
      confirm-button-label="Add"
      width="30%"
      :confirmButtonDisabled="!remoteAccessFormData.isFormValid"
      :confirm-loading="confirmLoading"
      @canceled="$emit('canceled');"
      @confirmed="onConfirmClicked"
  >
    <template #content>
      <RemoteAccessForm
        v-model="remoteAccessFormData"
      />
    </template>
  </confirm-dialog>
</template>

<style scoped>

</style>