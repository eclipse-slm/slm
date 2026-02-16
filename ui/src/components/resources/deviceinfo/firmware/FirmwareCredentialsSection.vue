<script setup lang="ts">
import { computed, ref, watch } from "vue";
import { useToast } from "vue-toast-notification";
import RowWithLabel from "@/components/base/RowWithLabel.vue";
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";
import CredentialDisplay from "@/components/credentials/CredentialDisplay.vue";
import CredentialForm from "@/components/credentials/CredentialForm.vue";
import PlatformManagementClient from "@/api/platform-management/platform-management-client";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import { ResourceCredentialScope } from "@/api/resource-management/client";
import type {CredentialCreateRequest, CredentialReadDTO} from "@/api/platform-management/client";
import type { CredentialFormData } from "@/components/credentials/types";
import {useUserStore} from "@/stores/userStore";
import { SectionState, type SectionStateChangeEvent } from "@/components/resources/deviceinfo/firmware/sectionState";

const $toast = useToast();

const props = defineProps({
  resourceId: {
    type: String,
    required: true,
  },
});


const emit = defineEmits<{ stateChanged: [SectionStateChangeEvent] }>();

const credentialsLoading = ref(false);
const firmwareCredentials = ref<CredentialReadDTO[]>([]);
const showDeleteCredentialDialog = ref(false);
const credentialToDeleteId = ref<string | null>(null);
const showCredentialDialog = ref(false);
const credentialFormData = ref<CredentialFormData | null>(null);
const submittingCredential = ref(false);
const deleteCredentialIfOrphaned = ref(false);
const deleteCredentialLoading = ref(false);

const userStore = useUserStore();

watch(
  () => props.resourceId,
  () => {
    if (!props.resourceId) {
      emit("stateChanged", { state: SectionState.Loaded });
      return;
    }
    loadFirmwareCredentials();
  },
  { immediate: true }
);


function loadFirmwareCredentials() {
  credentialsLoading.value = true;
  emit("stateChanged", { state: SectionState.Loading });
  PlatformManagementClient.credentialsApi
    .getCredentialsOfEntity(props.resourceId, "RESOURCE")
    .then((response) => {
      const all = response?.data ?? [];
      firmwareCredentials.value = all.filter((c: CredentialReadDTO) =>
        Array.isArray(c.scopesRaw) &&
        c.scopesRaw.includes(ResourceCredentialScope.FirmwareUpdate)
      );
    })
    .catch((e) => {
      console.error("Error loading credentials:", e);
      $toast.error("Failed to load firmware credentials");
      emit("stateChanged", { state: SectionState.Error, message: "Failed to load firmware credentials" });
    })
    .finally(() => {
      credentialsLoading.value = false;
      emit("stateChanged", { state: SectionState.Loaded });
    });
}

const firstFirmwareCredential = computed(
  () => firmwareCredentials.value[0] ?? null
);

function requestDeleteCredential(credentialId: string) {
  credentialToDeleteId.value = credentialId;
  deleteCredentialIfOrphaned.value = false;
  showDeleteCredentialDialog.value = true;
}

// Function to unassign a credential from firmware update
async function unassignFirmwareUpdateCredential(credentialId: string, deleteOrphaned: boolean) {
  try {
    await ResourceManagementClient.resourcesUpdatesApi.unassignFirmwareUpdateCredentialFromResource(props.resourceId, credentialId, deleteOrphaned);
    $toast.success("Credential unassigned successfully from firmware update.");
    loadFirmwareCredentials();
  } catch (error) {
    console.error("Error unassigning credential:", error);
    $toast.error("Failed to unassign credential from firmware update.");
  }
}

function confirmDeleteCredential() {
  if (!credentialToDeleteId.value) return;
  deleteCredentialLoading.value = true;
  unassignFirmwareUpdateCredential(credentialToDeleteId.value, deleteCredentialIfOrphaned.value)
    .finally(() => {
      deleteCredentialLoading.value = false;
      showDeleteCredentialDialog.value = false;
      credentialToDeleteId.value = null;
    });
}

// Function to assign a credential to firmware update
async function assignFirmwareUpdateCredential(credentialId: string) {
  try {
    await ResourceManagementClient.resourcesUpdatesApi.assignFirmwareUpdateCredentialToResource(props.resourceId, credentialId);
    $toast.success("Credential assigned successfully for firmware update.");
    loadFirmwareCredentials();
  } catch (error) {
    console.error("Error assigning credential:", error);
    $toast.error("Failed to assign credential for firmware update.");
  }
}

// Updated function to handle creation and assignment
async function handleCredentialSubmission() {
  if (!credentialFormData.value) return;

  submittingCredential.value = true;
  try {
    if (credentialFormData.value.useExisting && credentialFormData.value.existingCredentialId) {
      // Assign existing credential
      await assignFirmwareUpdateCredential(credentialFormData.value.existingCredentialId);
    } else if (credentialFormData.value.data) {
      // Create new credential and assign
      const credentialId = globalThis.crypto?.randomUUID?.();
      if (!credentialId) {
        throw new Error('Unable to generate credential id');
      }
      const credentialCreateRequest = {
        credential: {
          id: credentialId,
          scopesRaw: [ResourceCredentialScope.FirmwareUpdate],
          data: credentialFormData.value.data,
        },
        entityLinks: [{
          entityId: props.resourceId,
          entityType: "RESOURCE",
        }],
        fullPathOwnerGroupId: userStore.fullPathUserGroupId
      } as CredentialCreateRequest;
      const response = await PlatformManagementClient.credentialsApi.createOrUpdateCredential(credentialId, credentialCreateRequest);
      await assignFirmwareUpdateCredential(credentialId);
    }
    $toast.success("Credential for firmware update successfully added.");
  } catch (error) {
    console.error("Error adding credential for firmware update:", error);
    $toast.error("Failed to add credential for firmware update.");
  } finally {
    submittingCredential.value = false;
    showCredentialDialog.value = false;
    loadFirmwareCredentials();
  }
}

function openCredentialDialog() {
  credentialFormData.value = {
    isFormValid: false,
    useExisting: false,
    existingCredentialId: undefined,
    data: undefined,
  };
  showCredentialDialog.value = true;
}
</script>

<template>
  <RowWithLabel label="Credentials">
    <template #content>
      <div v-if="credentialsLoading">
        <progress-circular
          size="20"
          width="2"
        />
      </div>
      <div v-else>
        <div v-if="firmwareCredentials.length === 0">
          No credentials defined for firmware updates
          <v-btn
            small
            color="secondary"
            class="ml-2"
            @click="openCredentialDialog"
          >
            <v-icon color="white">mdi-plus</v-icon>
          </v-btn>
        </div>
        <div v-else>
          <div style="display: flex; align-items: center; justify-content: flex-end; width: 100%;">
            <v-btn
              size="small"
              color="secondary"
              @click.stop="requestDeleteCredential(firstFirmwareCredential.id)"
            >
              <v-icon color="white">mdi-delete</v-icon>
            </v-btn>
          </div>
          <div class="mt-2">
            <CredentialDisplay :credential="firstFirmwareCredential" />
          </div>
        </div>
      </div>
    </template>
  </RowWithLabel>

  <ConfirmDialog
    :show="showCredentialDialog"
    title="Add credential for firmware update"
    confirm-button-label="Add"
    cancel-button-label="Cancel"
    :confirm-loading="submittingCredential"
    @confirmed="handleCredentialSubmission"
    @canceled="showCredentialDialog = false"
    :width="'30%'"
  >
    <template #content>
      <CredentialForm
        :allow-existing="true"
        @changed="credentialFormData = $event"
      />
    </template>
  </ConfirmDialog>

  <ConfirmDialog
    :show="showDeleteCredentialDialog"
    title="Remove credential for firmware update"
    confirm-button-label="Remove"
    cancel-button-label="Cancel"
    :attention="true"
    :confirm-loading="deleteCredentialLoading"
    @confirmed="confirmDeleteCredential"
    @canceled="showDeleteCredentialDialog = false"
  >
    <template #content>
      <div>
        <div class="mb-4">Do you want to remove this credential for firmware update?</div>
        <v-checkbox
          v-model="deleteCredentialIfOrphaned"
          label="Delete credential if it becomes orphaned"
          density="comfortable"
          hide-details
        />
      </div>
    </template>
  </ConfirmDialog>
</template>
