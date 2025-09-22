<script setup lang="ts">
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import { useClipboard } from '@vueuse/core'
import {onMounted, ref} from "vue";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import RowWithLabel from "@/components/base/RowWithLabel.vue";
import {useToast} from 'vue-toast-notification';
import DeviceUtils from '@/utils/deviceUtils';
import RemoteAccessDialog from "@/components/resources/remoteaccess/RemoteAccessDialog.vue";
import {RemoteAccessDTO, ResourceDTO} from "@/api/resource-management/client";
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";
import logRequestError from "@/api/restApiHelper";

const props = defineProps({
  resourceId: {
    type: String,
    default: ""
  }
});

const resourceDevicesStore = useResourceDevicesStore();
const $toast = useToast();

const resource = ref<ResourceDTO|undefined>(undefined)
const remoteAccess = ref<RemoteAccessDTO[]>([])
const showRemoteAccessCreateOrUpdateDialog = ref(false)
const showRemoteAccessDeleteDialog = ref(false)
const showPassword = ref(false);


const loading = ref(true);
const loadData = () => {
  // Get resource
  ResourceManagementClient.resourcesApi.getResource(props.resourceId).then(response => {
    resource.value = response.data
    // Load remote access
    const promises = (resource.value?.remoteAccessIds ?? []).map(remoteAccessId =>
        ResourceManagementClient.resourcesApi.getRemoteAccessOfResourceById(resource.value!.id, remoteAccessId)
    );
    Promise.all(promises).then(responses => {
      remoteAccess.value = responses.map(r => r.data);
    }).finally(() => {
      loading.value = false
    });

  }).catch((e) => {
    resource.value = undefined;
    loading.value = false
  })
}

const onRemoteAccessCreateOrEditDialogConfirmed = () => {
  showRemoteAccessCreateOrUpdateDialog.value = false
  loadData()
}

const onDeleteRemoteAccessDialogConfirmed = (remoteAccessId: string) => {
  ResourceManagementClient.resourcesApi.deleteRemoteAccessOfResourceById(resource.value!.id, remoteAccessId).then(() => {
    loadData()
    showRemoteAccessDeleteDialog.value = false
    $toast.info("Remote access deleted")
  }).catch((e) => {
    logRequestError(e)
    $toast.error("Failed to delete remote access")
  })
}

const { copy, isSupported } = useClipboard()
const copyPassword = (remoteAccessId) => {
  const selectedRemoteAccess = remoteAccess.value.find(ra => ra.id === remoteAccessId);
  if (selectedRemoteAccess?.credential?.password !== undefined) {
    copy(selectedRemoteAccess?.credential.password)
    $toast.info(`Password copied to clipboard`)
  }
  else {
    $toast.info(`No password available`)
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <v-container fluid>
    <div v-if="loading">
      <progress-circular />
    </div>

    <div v-else>
      <RowWithLabel
        label="Product"
        :text="DeviceUtils.getProduct(resourceId)"
      />
      <RowWithLabel
        label="Vendor"
        :text="DeviceUtils.getManufacturer(resourceId)"
      />
      <RowWithLabel
        label="Resource Id"
        :text="resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId, 'DeviceInfo', '$.Id')"
      />
      <RowWithLabel
        label="Asset Id"
        :text="resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId, 'DeviceInfo', '$.AssetId')"
      />
      <RowWithLabel
        label="Hostname"
        :text="resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId, 'DeviceInfo', '$.Hostname')"
      />
      <RowWithLabel
        label="IP"
        :text="resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId, 'DeviceInfo', '$.IP')"
      />
      <RowWithLabel
        label="Remote Access"
      >
        <template #content>
          <div v-if="!resource?.remoteAccessAvailable">
            <v-btn color="primary" @click="showRemoteAccessCreateOrUpdateDialog = true">
              <v-icon color="white">
                mdi-plus
              </v-icon>
            </v-btn>
            <RemoteAccessDialog
                :show="showRemoteAccessCreateOrUpdateDialog"
                :resource-id="resourceId"
                width="35%"
                @canceled="showRemoteAccessCreateOrUpdateDialog = false"
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
                  v-for="(item, idx) in remoteAccess"
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
                          color="error"
                          @click.stop="showRemoteAccessDeleteDialog = true"
                      >
                        <v-icon
                            icon="mdi-trash-can"
                            color="white"
                        />
                      </v-btn>
                      <confirm-dialog
                          :show="showRemoteAccessDeleteDialog"
                          title="Delete remote access"
                          :text="`Do you want to delete remote access '${item.connectionType}'?`"
                          confirm-button-label="Delete"
                          @canceled="showRemoteAccessDeleteDialog = false"
                          @confirmed="onDeleteRemoteAccessDialogConfirmed(item.id)"
                      ></confirm-dialog>
                    </v-col>
                  </v-row>
                </template>
                <template #text>
                  <RowWithLabel
                    label="Port"
                    :text="item.connectionPort"
                  />
                  <RowWithLabel
                    label="Username"
                    :text="item.credential.username"
                  />
                  <RowWithLabel
                    label="Password"
                  >
                    <template #content>
                      <v-row>
                        <v-col cols="8">
                          <div v-if="showPassword">
                            {{ item.credential.password }}
                          </div>
                          <div v-else>
                            ••••••••
                          </div>
                        </v-col>
                        <v-col cols="1">
                          <v-btn
                            color="info"
                            @click="showPassword = !showPassword"
                          >
                            <v-icon
                              :icon="showPassword ? 'mdi-glasses' : 'mdi-sunglasses'"
                              color="white"
                            />
                          </v-btn>
                        </v-col>
                        <v-col
                          v-if="isSupported"
                          cols="1"
                        >
                          <v-btn
                            color="info"
                            @click="copyPassword(item.id)"
                          >
                            <v-icon icon="mdi-content-copy" />
                          </v-btn>
                        </v-col>
                      </v-row>
                    </template>
                  </RowWithLabel>
                </template>
              </v-expansion-panel>
            </v-expansion-panels>
          </div>
        </template>
      </RowWithLabel>
    </div>
  </v-container>
</template>

<style scoped>
.v-divider {
  border-color: #000000;
}
</style>