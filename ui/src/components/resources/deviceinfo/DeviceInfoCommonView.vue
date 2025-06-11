<script setup lang="ts">
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import { useClipboard } from '@vueuse/core'
import {onMounted, ref} from "vue";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import {BasicResource} from "@/api/resource-management/client";
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import RowWithLabel from "@/components/base/RowWithLabel.vue";
import {useToast} from 'vue-toast-notification';
import DeviceUtils from '@/utils/deviceUtils';

const props = defineProps({
  resourceId: {
    type: String,
    default: ""
  }
});

const resourceDevicesStore = useResourceDevicesStore();
const $toast = useToast();

const resource = ref<BasicResource|undefined>(undefined)

const showPassword = ref(false);

const loading = ref(true);
const loadData = () => {
  // Get aas descriptor
  ResourceManagementClient.resourcesApi.getResource(props.resourceId).then(response => {
    resource.value = response.data
  }).catch((e) => {
    resource.value = undefined;

  }).finally(() => {
    loading.value = false
  })
}

const { copy, isSupported } = useClipboard()
const copyPassword = () => {
  if (resource.value?.remoteAccessService?.credential?.password !== undefined) {
    copy(resource.value.remoteAccessService.credential.password)
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
        :text="DeviceUtils.getVendor(resourceId)"
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
          <div v-if="resource.remoteAccessService === undefined">
            Not available
          </div>
          <div v-else>
            <v-expansion-panels
              variant="accordion"
              flat
              :model-value="0"
            >
              <v-expansion-panel
                :title="resource.remoteAccessService.connectionType"
                expand
              >
                <template #text>
                  <RowWithLabel
                    label="Port"
                    :text="resource.remoteAccessService.Port"
                  />
                  <RowWithLabel
                    label="Username"
                    :text="resource.remoteAccessService.credential.username"
                  />
                  <RowWithLabel
                    label="Password"
                  >
                    <template #content>
                      <v-row>
                        <v-col cols="8">
                          <div v-if="showPassword">
                            {{ resource.remoteAccessService.credential.password }}
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
                            @click="copyPassword"
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