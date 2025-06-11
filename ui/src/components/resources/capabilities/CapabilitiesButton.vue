<script setup lang="ts">

import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";
import {useCapabilityUtils} from "@/utils/capabilityUtils";
import CapabilityParamsDialog from "@/components/resources/capabilities/CapabilityParamsDialog.vue";
import {ref} from "vue";

const emit = defineEmits(['closed']);

const props = defineProps({
  resource: {
    type: Object,
    required: true
  }
})

const selectedCapabilityId = ref(undefined)
const selectedSkipInstall = ref(false)
const showCapabilityParamsDialog = ref(false)

const resourceDevicesStore = useResourceDevicesStore();
const capabilityUtils = useCapabilityUtils();

const insertWhiteSpaceInCamelCase = (string) => {
  return string.replace(/([A-Z])/g, ' $1').trim()
}

const addCapability = (capabilityId, skipInstall, configParameterMap) => {
  ResourceManagementClient.capabilityApi.installCapabilityOnSingleHost(props.resource.id, capabilityId, configParameterMap, skipInstall)
      .then()
      .catch(logRequestError)
}

const removeCapability = (capabilityId) => {
  ResourceManagementClient.capabilityApi.removeCapabilityFromSingleHost(props.resource.id, capabilityId)
      .then().catch(logRequestError);

}

const onInstallButtonClicked = (capabilityId, skipInstall) => {
  if(capabilityUtils.isDefineCapabilityDialogRequired(capabilityId, skipInstall)) {
    selectedCapabilityId.value = capabilityId
    selectedSkipInstall.value = skipInstall
    showCapabilityParamsDialog.value = true
  }
  else {
    addCapability(capabilityId,skipInstall,{})
  }
}

const onCapabilityParamsDialogCanceled = () => {
  showCapabilityParamsDialog.value = false;
};

const onCapabilityParamsDialogConfirmed = (capabilityParametersMap) => {
  addCapability(selectedCapabilityId.value, selectedSkipInstall.value, capabilityParametersMap)
  showCapabilityParamsDialog.value = false;
};
</script>

<template>
  <div>
    <v-menu>
      <template #activator="{ props }">
        <v-btn
          id="mushroom-button"
          color="info"
          size="small"
          v-bind="props"
          :disabled="resource.clusterMember || resourceDevicesStore.availableSingleHostCapabilitiesNoDefault.length === 0"
        >
          <v-icon
            color="white"
            icon="mdi-mushroom-outline"
          />
        </v-btn>
      </template>
      <v-list>
        <v-list
          v-for="capabilityClass in capabilityUtils.getUniqueCapabilityClasses()"
          :key="capabilityClass"
          class="resources-capability-menu"
        >
          <h4 class="ml-4">
            {{ insertWhiteSpaceInCamelCase(capabilityClass) }}
          </h4>
          <v-list-item
            v-for="capability in capabilityUtils.getCapabilitiesByCapabilityClass(capabilityClass)"
            :key="capability.id"
          >
            <v-btn-toggle>
              <!-- Install Button -->
              <v-btn
                v-if="!capabilityUtils.isCapabilityInstalledOnResource(resource,capability)"
                :disabled="resource.remoteAccessService == null"
                size="small"
                base-color="info"
                variant="flat"
                :prepend-icon="capability.logo"
                @click="onInstallButtonClicked(capability.id, false)"
              >
                {{ capability.name }}
              </v-btn>
              <!-- Skip Install Button -->
              <v-btn
                v-if="!capabilityUtils.isCapabilityInstalledOnResource(resource,capability) && capabilityUtils.isCapabilitySkipable(capability)"
                base-color="info"
                variant="flat"
                size="small"
                @click="onInstallButtonClicked(capability.id, true)"
              >
                <v-icon icon="mdi-skip-next" />
              </v-btn>
              <!-- Uninstall Button -->
              <v-btn
                v-if="capabilityUtils.isCapabilityInstalledOnResource(resource, capability)"
                base-color="error"
                size="small"
                variant="flat"
                :prepend-icon="capability.logo"
                @click="removeCapability(capability.id)"
              >
                {{ capability.name }}
              </v-btn>
            </v-btn-toggle>
          </v-list-item>
        </v-list>
      </v-list>
    </v-menu>

    <capability-params-dialog
      :show="showCapabilityParamsDialog"
      :capability-id="selectedCapabilityId"
      :resource-id="resource.id"
      :skip-install="selectedSkipInstall"
      @confirmed="onCapabilityParamsDialogConfirmed"
      @canceled="onCapabilityParamsDialogCanceled"
    />
  </div>
</template>

<style scoped>

</style>