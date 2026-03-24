<template>
  <div>
    <div v-if="loading">
      <progress-circular />
    </div>

    <div v-else>
      <v-data-table
        v-if="submodels.length > 0"
        :items="submodels"
        :headers="headers"
        item-value="id"
      >
        <template #item.idShort="{ item }">
          {{ item.idShort }}
        </template>
        <template #item.id="{ item }">
          {{ item.id }}
        </template>
        <template #item.semanticId="{ item }">
          <div v-if="item.semanticId">
            {{ item.semanticId.keys[0].value }}
          </div>
        </template>
        <template #item.actions="{ item }">
          <v-btn
            color="info"
            class="ma-2"
          >
            <a
              v-if="aasDescriptor"
              :href="`${aasGuiUrl}/?aas=${aasDescriptor.endpoints[0].protocolInformation.href}&path=${item.endpoints[0].protocolInformation.href}`"
              target="_blank"
            >
              <v-icon color="white">mdi-open-in-new</v-icon>
            </a>
          </v-btn>
          <v-btn
            color="error"
            class="ma-2"
            @click.stop="submodelToDelete = item"
          >
            <v-icon icon="mdi-delete" />
          </v-btn>
        </template>
      </v-data-table>

      <div v-else>
        <v-alert
          variant="outlined"
          type="info"
        >
          {{ 'No submodels found.' }}
        </v-alert>
      </div>
    </div>

    <v-row align="center">
      <v-col>
        <v-file-input
          v-model="file"
          accept=".aasx"
          :placeholder="'Select .aasx file'"
          variant="underlined"
        />
      </v-col>
      <v-col>
        <v-btn
          color="info"
          :disabled="!file"
          @click="addSubmodels"
        >
          <v-icon
            icon="mdi-upload"
            color="white"
          />
        </v-btn>
      </v-col>
    </v-row>
    <confirm-dialog
      :show="submodelToDelete !== undefined"
      :title="'Delete submodel ' + (submodelToDelete == null ? '' : submodelToDelete.idShort)"
      text="Do you want to delete this submodel?"
      @confirmed="deleteSubmodel(submodelToDelete)"
      @canceled="submodelToDelete = null"
    />
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted, computed} from 'vue'
import ConfirmDialog from '@/components/base/ConfirmDialog.vue'
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import {AssetAdministrationShellDescriptor} from "@/api/resource-management/client";
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import {useEnvStore} from "@/stores/environmentStore";

const props = defineProps({
  resourceId: {
    type: String,
    default: ""
  }
})

const envStore = useEnvStore();

const aasDescriptor = ref<AssetAdministrationShellDescriptor>({})
const submodels = ref<any>([]);
const submodelToDelete = ref<string|undefined>(undefined)
const file = ref<File|undefined>(undefined)
const aasGuiUrl = envStore.basyxAasGuiUrl;
const headers = [
  { title: 'Id Short', value: 'idShort', sortable: true, width: '25%' },
  { title: 'Id', value: 'id', sortable: true, width: '30%' },
  { title: 'Semantic Id', value: 'semanticId', sortable: true, width: '30%' },
  { title: 'Actions', value: 'actions', sortable: false, width: '15%' },
]

const loading_aasDescriptor = ref(true);
const loading_submodels = ref(true);
const loading = computed(() => loading_aasDescriptor.value && loading_submodels);
const loadData = () => {
  // Get aas descriptor
  ResourceManagementClient.aasApi.getResourceAasDescriptor(props.resourceId).then(response => {
    aasDescriptor.value = response.data
  }).catch((e) => {
    aasDescriptor.value = {};

  }).finally(() => {
    loading_aasDescriptor.value = false
  })

  // Get submodels
  ResourceManagementClient.submodelsApi.getResourceSubmodels(props.resourceId).then(response => {
    if (response.data && response.data.length === 1 && Object.keys(response.data).length === 0) {
      return
    }
    submodels.value = response.data

  }).catch((e) => {
    console.log(e)
    submodels.value = []
  }).finally(() => {
    loading_submodels.value = false
  })
}

const deleteSubmodel = (submodel) => {
  let submodelIdBase64Encoded = btoa(submodel.id);
  ResourceManagementClient.submodelsApi.deleteSubmodel(props.resourceId, submodelIdBase64Encoded).then(response => {
    loadData()
    submodelToDelete.value = undefined
  }).catch((e) => {
    console.log(e)
  })
}

const addSubmodels = () => {
  if (file.value == undefined) {
    return
  }
  ResourceManagementClient.submodelsApi.addSubmodels(props.resourceId, file.value).then(response => {
    file.value = undefined
    loadData()
  }).catch((e) => {
    console.log(e)
  })
}

onMounted(() => {
  loadData()
})
</script>