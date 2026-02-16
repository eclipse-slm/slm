<script setup lang="ts">

import {computed, onMounted, ref} from 'vue';
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import {ResourceCredentialReadDTO, ResourceCredentialScope} from "@/api/resource-management/client";
import { useToast } from 'vue-toast-notification';
import ConfirmDialog from '@/components/base/ConfirmDialog.vue';
import PlatformManagementClient from "@/api/platform-management/platform-management-client";
import CredentialDisplay from '@/components/credentials/CredentialDisplay.vue';

const props = defineProps({
  resourceId: {
    type: String,
    required: true,
  },
});

const credentials = ref<ResourceCredentialReadDTO[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const selectedScope = ref<ResourceCredentialScope | null>(null);

const showConfirmDeleteDialog = ref(false);

const scopeOptions = [ResourceCredentialScope.RemoteAccess, ResourceCredentialScope.FirmwareUpdate];

const selectedCredentialIds = ref<string[]>([]);

const tableHeaders = [
  { title: 'Type', value: 'data.credentialDataType' },
  { title: 'Scopes', value: 'scopes' },
  { title: 'ID', value: 'id' },
];
const deleting = ref(false);

const $toast = useToast();

async function deleteSelectedCredentials() {
  if (!selectedCredentialIds.value.length) return;
  deleting.value = true;
  try {
    await Promise.allSettled(
      selectedCredentialIds.value.map((id) => PlatformManagementClient.credentialsApi.deleteCredential(id))
    );
    await loadCredentialsOfResource();
    selectedCredentialIds.value = [];
    $toast.success('Credentials deleted');
  } catch (_e) {
    console.error('Error deleting credentials', _e);
    $toast.error('Error deleting credentials');
  } finally {
    deleting.value = false;
  }
}

function onDeleteClicked() {
  if (!selectedCredentialIds.value.length) return;
  showConfirmDeleteDialog.value = true;
}

async function onConfirmDelete() {
  showConfirmDeleteDialog.value = false;
  await deleteSelectedCredentials();
}

onMounted(() => {
  loadCredentialsOfResource()
});

function loadCredentialsOfResource() {
  loading.value = true;
  ResourceManagementClient.credentialsApi.getCredentialsOfResource(props.resourceId)
      .then((response) => {
        credentials.value = response.data ?? [];
      })
      .catch((err) => {
        error.value = err?.message ?? 'Error loading credentials';
      })
      .finally(() => {
        loading.value = false;
      });
}

const filteredCredentials = computed(() => {
  // If no scope selected, show all
  if (!selectedScope.value) return credentials.value;
  const sel = String(selectedScope.value);
  return credentials.value.filter((c) => {
    const scopesArr: string[] = (c as any).scopesRaw ?? (c as any).scopes ?? [];
    return Array.isArray(scopesArr) && scopesArr.includes(sel);
  });
});

</script>

<template>
  <div>
    <v-row>
      <v-col cols="10">
      </v-col>
      <v-col cols="2" class="d-flex align-center justify-end ga-2">
        <v-btn small  color="secondary"  :disabled="!selectedCredentialIds.length || deleting" @click="onDeleteClicked">
          <v-icon color="white">mdi-delete</v-icon>
        </v-btn>
      </v-col>
    </v-row>

    <v-alert v-if="error" type="error" variant="tonal" density="comfortable">
      {{ error }}
    </v-alert>

    <div v-else>
      <v-select
          v-model="selectedScope"
          :items="scopeOptions"
          label="Credential Scope"
          class="my-8"
          density="comfortable"
          clearable
          hide-details
          placeholder="Keinen Filter (alle)"
      />

      <div class="d-flex justify-center my-6" v-if="loading">
        <v-progress-circular indeterminate color="primary" />
      </div>

      <v-alert v-else-if="!filteredCredentials.length" type="info" variant="tonal" density="comfortable">
        No credentials found for this resource.
      </v-alert>

      <div v-else>
        <!-- Data table with expandable rows -->
        <v-data-table
          :items="filteredCredentials"
          :headers="tableHeaders"
          item-key="id"
          item-value="id"
          show-expand
          class="elevation-1"
          :model-value="selectedCredentialIds"
          @update:model-value="val => selectedCredentialIds = val"
          show-select
        >
          <template #item.scopes="{ item }">
            <v-chip-group>
              <v-chip
                v-for="scope in ((item as any).scopesRaw ?? (item as any).scopes ?? [])"
                :key="scope"
                size="small"
                color="secondary"
                variant="tonal"
                class="ma-1"
              >
                {{ scope }}
              </v-chip>
            </v-chip-group>
          </template>

          <template #expanded-row="{ item }">
            <tr>
              <td colspan="3">
                 <v-card flat>
                   <v-card-text>
                    <credential-display
                      :credential="item"
                    />
                   </v-card-text>
                 </v-card>
               </td>
             </tr>
           </template>
         </v-data-table>
      </div>
    </div>

    <!-- Confirm delete dialog -->
    <ConfirmDialog
      :show="showConfirmDeleteDialog"
      title="Delete credentials"
      :text="`Do you want to delete ${selectedCredentialIds.length} credential(s)?`"
      confirm-button-label="Delete"
      cancel-button-label="Cancel"
      :attention="true"
      @canceled="showConfirmDeleteDialog = false"
      @confirmed="onConfirmDelete"
    />
  </div>
</template>

<style scoped>
pre {
  background: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
  overflow-x: auto;
}

</style>

