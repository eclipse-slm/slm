<template>
  <div>
    <v-table v-slot v-if="submodels.length > 0">
      <thead>
        <tr>
          <th>{{ 'IdShort' }}</th>
          <th>{{ 'Identification' }}</th>
          <th>{{ 'Details' }}</th>
          <th>{{ 'semantic Id' }}</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="submodel in submodels" :key="submodel.idShort">
          <td> {{ submodel.idShort }}</td>
          <td> {{ submodel.identification.idType }}, {{ submodel.identification.id }} </td>
          <td>
            <a :href="aasGuiUrl+'/?aas='+submodel.endpoints[0].address.replace('aas/submodels', 'aas&path=submodels')" target="_blank"><v-icon>mdi-open-in-new</v-icon></a>
          </td>
          <td>
            <div v-if="submodel.semanticId && submodel.semanticId.keys.length > 0">
              {{ submodel.semanticId.keys[0].idType }}, {{ submodel.semanticId.keys[0].value }}
            </div>
          </td>
          <td>
            <v-btn color="error" @click.stop="submodelToDelete = submodel"><v-icon>mdi-delete</v-icon></v-btn>
          </td>
        </tr>
      </tbody>
    </v-table>
    <div v-else>
      <v-alert variant="outlined" type="info">
        {{ 'No submodels found.' }}
      </v-alert>
    </div>
    <v-row class="align-center">
      <v-col>
        <v-file-input accept=".aasx" v-model="file" :placeholder="'select .aasx file'"></v-file-input>
      </v-col>
      <v-col>
        <v-btn color="info" @click="addSubmodels" :disabled="!file">
          <v-icon>mdi-upload</v-icon> {{ 'Upload submodel AASX package' }}
        </v-btn>
      </v-col>
    </v-row>
    <confirm-dialog :isActive="submodelToDelete != null"
      :title="'Delete submodel ' + (submodelToDelete == null ? '' : submodelToDelete.idShort)"
      text="Do you really want to delete this submodel?" @confirmed="deleteSubmodel(submodelToDelete)"
      @canceled="submodelToDelete = null" />
  </div>
</template>
<script>
import SubmodelsRestApi from '@/api/resource-management/submodelsRestApi'
import ConfirmDialog from '@/components/base/ConfirmDialog'
import getEnv from '@/utils/env'

export default {
  name: 'ResourceSubmodels',
  components: { ConfirmDialog },
  props: ['resourceId'],
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
      SubmodelsRestApi.getResourceSubmodels(this.resourceId).then(response => {
        if (response.length === 1 && Object.keys(response).length === 0) {
          return
        }
        this.submodels = response
      }).catch((e) => {
        console.log(e)
        this.submodels = []
      })
    },
    deleteSubmodel(submodel) {
      SubmodelsRestApi.deleteSubmodel(this.resourceId, submodel.idShort).then(response => {
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
      SubmodelsRestApi.addSubmodels(this.resourceId, this.file).then(response => {
        this.file = null
        this.getSubmodels()
      }).catch((e) => {
        console.log(e)
      })
    }
  },
}
</script>
