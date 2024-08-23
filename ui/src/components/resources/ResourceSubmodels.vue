<template>
  <div>
    <v-table
      v-if="submodels.length > 0"
      v-slot
    >
      <thead>
        <tr>
          <th>{{ 'IdShort' }}</th>
          <th>{{ 'Identification' }}</th>
          <th>{{ 'Details' }}</th>
          <th>{{ 'semantic Id' }}</th>
          <th />
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="submodel in submodels"
          :key="submodel.idShort"
        >
          <td> {{ submodel.idShort }}</td>
          <td> {{ submodel.identification.idType }}, {{ submodel.identification.id }} </td>
          <td>
            <a
              :href="aasGuiUrl+'/?aas='+submodel.endpoints[0].address.replace('aas/submodels', 'aas&path=submodels')" 
              target="_blank"
            ><v-icon>mdi-open-in-new</v-icon></a>
          </td>
          <td>
            <div v-if="submodel.semanticId && submodel.semanticId.keys.length > 0">
              {{ submodel.semanticId.keys[0].idType }}, {{ submodel.semanticId.keys[0].value }}
            </div>
          </td>
          <td>
            <v-btn 
              color="error"
              @click.stop="submodelToDelete = submodel"
            >
              <v-icon icon="mdi-delete" />
            </v-btn>
          </td>
        </tr>
      </tbody>
    </v-table>
    <div v-else>
      <v-alert
        variant="outlined"
        type="info"
      >
        {{ 'No submodels found.' }}
      </v-alert>
    </div>
    <v-row class="align-center">
      <v-col>
        <v-file-input
          v-model="file"
          accept=".aasx"
          :placeholder="'select .aasx file'"
        />
      </v-col>
      <v-col>
        <v-btn 
          color="info" 
          icon="mdi-upload" 
          :disabled="!file" 
          @click="addSubmodels"
        >
          {{ 'Upload submodel AASX package' }}
        </v-btn>
      </v-col>
    </v-row>
    <confirm-dialog
      :show="submodelToDelete !== null"
      :title="'Delete submodel ' + (submodelToDelete == null ? '' : submodelToDelete.idShort)"
      text="Do you really want to delete this submodel?"
      @confirmed="deleteSubmodel(submodelToDelete)"
      @canceled="submodelToDelete = null"
    />
  </div>
</template>
<script>
import ConfirmDialog from '@/components/base/ConfirmDialog'
import getEnv from '@/utils/env'
import ResourceManagementClient from "@/api/resource-management/resource-management-client";

export default {
  name: 'ResourceSubmodels',
  components: { ConfirmDialog },
  props: {
    resourceId: {
      type: String,
      default: ""
    }
  },
  data() {
    return {
      submodels: [],
      submodelToDelete: null,
      file: null,
      aasGuiUrl: getEnv("VUE_APP_BASYX_AAS_GUI_URL")
    }
  },

  mounted() {
    this.getSubmodels()
  },

  methods: {
    getSubmodels() {
      ResourceManagementClient.submodelsApi.getResourceSubmodels(this.resourceId).then(response => {
        if (response.data && response.data.length === 1 && Object.keys(response.data).length === 0) {
          return
        }
        this.submodels = response.data
      }).catch((e) => {
        console.log(e)
        this.submodels = []
      })
    },
    deleteSubmodel(submodel) {
      ResourceManagementClient.submodelsApi.deleteSubmodel(this.resourceId, submodel.idShort).then(response => {
        this.getSubmodels()
        this.submodelToDelete = null
      }).catch((e) => {
        console.log(e)
      })
    },
    addSubmodels() {
      if (this.file == null) {
        return
      }
      ResourceManagementClient.submodelsApi.addSubmodels(this.resourceId, this.file).then(response => {
        this.file = null
        this.getSubmodels()
      }).catch((e) => {
        console.log(e)
      })
    }
  },
}
</script>
