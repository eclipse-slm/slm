<script setup lang="ts">
import { ref } from "vue";
import { useToast } from "vue-toast-notification";
import { useClipboard } from "@vueuse/core";
import RowWithLabel from "@/components/base/RowWithLabel.vue";
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import RemoteAccessDialog from "@/components/resources/remoteaccess/RemoteAccessDialog.vue";
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";
import CredentialDisplay from "@/components/credentials/CredentialDisplay.vue";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";
import {RemoteAccessDTOReadFull, ResourceDTO} from "@/api/resource-management/client";
import { CredentialDataType } from "@/api/platform-management/client";

const props = defineProps({
  resourceId: {
    type: String,
    required: true,
  },
});

const emit = defineEmits(["stateChanged"]);

const $toast = useToast();
const { copy, isSupported } = useClipboard();

const resource = ref<ResourceDTO | undefined>(undefined);
const remoteAccesses = ref<RemoteAccessDTOReadFull[]>([]);

const createDialog = ref({
  open: false,
  loading: false,
});

const deleteDialog = ref({
  open: false,
  loading: false,
  deleteCredentialIfOrphaned: false,
  targetRemoteAccessId: "" as string,
});

const loading = ref(true);

function loadData() {
  loading.value = true;
  emit("stateChanged", { state: "loading" });
  ResourceManagementClient.resourcesApi
    .getResource(props.resourceId)
    .then(response => {
      resource.value = response.data;
      remoteAccesses.value = [];
      const remoteAccessPromises = (resource.value?.remoteAccessIds ?? []).map(remoteAccessId =>
        ResourceManagementClient.resourcesApi.getRemoteAccessOfResourceById(resource.value!.id, remoteAccessId)
      );
      return Promise.all(remoteAccessPromises);
    })
    .then(remoteAccessResponses => {
      remoteAccesses.value = remoteAccessResponses.map(remoteAccessResponse => remoteAccessResponse.data);
       emit("stateChanged", { state: "loaded" });
    })
    .catch((e) => {
      logRequestError(e);
      emit("stateChanged", { state: "error", message: "Failed to load remote access data" });
    })
    .finally(() => {
      loading.value = false;
    });
}

const onRemoteAccessCreateOrEditDialogConfirmed = () => {
  createDialog.value.open = false;
  loadData();
}

function openDeleteRemoteAccessDialog(remoteAccessId: string) {
  deleteDialog.value.open = true;
  deleteDialog.value.loading = false;
  deleteDialog.value.deleteCredentialIfOrphaned = false;
  deleteDialog.value.targetRemoteAccessId = remoteAccessId;
}

const onDeleteRemoteAccessDialogConfirmed = () => {
  if (!deleteDialog.value.targetRemoteAccessId) {
    return;
  }
  deleteDialog.value.loading = true;
  ResourceManagementClient.resourcesApi
    .deleteRemoteAccessOfResourceById(
      resource.value!.id,
      deleteDialog.value.targetRemoteAccessId,
      deleteDialog.value.deleteCredentialIfOrphaned
    )
    .then(() => {
      loadData();
      deleteDialog.value.open = false;
      $toast.info("Remote access deleted");
    })
    .catch((e) => {
      logRequestError(e);
      $toast.error("Failed to delete remote access");
    })
    .finally(() => {
      deleteDialog.value.loading = false;
    });
}

function closeDeleteRemoteAccessDialog() {
  deleteDialog.value.open = false;
}

loadData();
</script>

<template>
  <RowWithLabel label="Remote Access">
    <template #content>
      <div v-if="loading">
        <progress-circular />
      </div>
      <div v-else>
        <div v-if="!resource?.remoteAccessAvailable">
          <v-btn color="primary" @click="createDialog.open = true">
            <v-icon color="white">
              mdi-plus
            </v-icon>
          </v-btn>
          <RemoteAccessDialog
            :show="createDialog.open"
            :resource-id="resourceId"
            width="35%"
            @canceled="createDialog.open = false"
            @confirmed="onRemoteAccessCreateOrEditDialogConfirmed"
          />
        </div>
        <div v-else>
          <v-expansion-panels
            variant="accordion"
            flat
            :model-value="0"
          >
            <v-expansion-panel
              v-for="(item, idx) in remoteAccesses"
              :key="item.id || idx"
              :title="item.connectionType"
              expand
            >
              <template #title>
                <v-row align="center" no-gutters>
                  <v-col cols="11">
                    <span class="ml-2">{{ item.connectionType }}</span>
                  </v-col>
                  <v-col cols="1">
                    <v-btn
                      color="secondary"
                      @click.stop="openDeleteRemoteAccessDialog(item.id)"
                    >
                      <v-icon
                        icon="mdi-trash-can"
                        color="white"
                      />
                    </v-btn>
                    <confirm-dialog
                      :show="deleteDialog.open"
                      title="Delete remote access"
                      cancel-button-label="Cancel"
                      confirm-button-label="Delete"
                      :attention="true"
                      :confirm-loading="deleteDialog.loading"
                      @canceled="closeDeleteRemoteAccessDialog"
                      @confirmed="onDeleteRemoteAccessDialogConfirmed"
                    >
                      <template #content>
                        <div>
                          <div class="mb-4">Do you want to delete remote access '{{ item.connectionType }}'?</div>
                          <v-checkbox
                            v-model="deleteDialog.deleteCredentialIfOrphaned"
                            label="Delete credential if it becomes orphaned"
                            density="comfortable"
                            hide-details
                          />
                        </div>
                      </template>
                    </confirm-dialog>
                  </v-col>
                </v-row>
              </template>
              <template #text>
                <RowWithLabel
                  label="Port"
                  :text="item.connectionPort"
                  :copyable="true"
                />
                <RowWithLabel
                  v-if="item.credential?.data?.credentialDataType !== CredentialDataType.UsernamePassword"
                  label="Username"
                  :text="item.username "
                  :copyable="true"
                />
                <credential-display
                  v-if="item.credential"
                  :credential="item.credential"
                  :hide-name="true"
                />
              </template>
            </v-expansion-panel>
          </v-expansion-panels>
        </div>
      </div>
    </template>
  </RowWithLabel>
</template>

<style scoped>
.copy-button {
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.15s ease-in-out;
}

.username-row:hover .copy-button {
  opacity: 1;
  pointer-events: auto;
}
</style>
