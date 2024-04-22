<template>
  <validation-observer
    ref="observer"
    v-slot="{ invalid, handleSubmit, validate }"
  >
    <v-card>
      <v-container
        fluid
        class="pa-8"
      >
        <validation-provider
          v-slot="{ errors }"
          name="Cluster Type"
          rules="required"
        >
          <v-row id="resource-create-select-cluster-type">
            <v-icon
              size="large"
              class="mr-7 ml-2"
            >
              mdi-selection-multiple
            </v-icon>
            <v-select
              v-model="selectedClusterType"
              :items="availableClusterTypesWithSkipInstall"
              item-title="name"
              label="Cluster Type"
              required
              return-object
              :error-messages="errors"

              @update:modelValue="onSelectedClusterTypeChanged"
            />
          </v-row>
        </validation-provider>
        <div
          v-for="configParameter in configParameters"
          :key="configParameter.name"
        >
          <v-row
            class="lign"
            justify="center"
          >
            <v-col cols="3">
              {{ configParameter.prettyName }}
              <v-tooltip
                location="bottom"
              >
                <template #activator="{ props }">
                  <v-icon
                    class="mx-3"
                    color="primary"
                    theme="dark"
                    v-bind="props"
                  >
                    mdi-information
                  </v-icon>
                </template>
                <span>{{ configParameter.description }}</span>
              </v-tooltip>
            </v-col>
            <v-col
              v-if="configParameter.valueType == 'FILE'"
              cols="9"
            >
              <v-file-input
                v-model="uploadedFiles[configParameter.name]"
                accept=".yml,.yaml"
                label="Click here to select file"
                auto-grow
                density="compact"
                variant="outlined"
                @update:modelValue="onFileChanged(configParameter.name)"
              />
            </v-col>
            <v-col
              v-if="configParameter.valueType == 'STRING'"
              cols="9"
            >
              <v-text-field
                v-model="configParameterValues[configParameter.name]"
                :label="configParameter.prettyName"
                clearable
              />
            </v-col>
            <v-col
                v-if="configParameter.valueType == 'KUBE_CONF'"
                cols="9"
            >
              <v-file-input
                  v-model="uploadedFiles[configParameter.name]"
                  accept=".yml,.yaml"
                  label="Click here to select kube config file"
                  auto-grow
                  density="compact"
                  variant="outlined"
                  @update:modelValue="onFileChanged(configParameter.name)"
              />
            </v-col>
          </v-row>
          <validation-provider
            v-if="configParameter.valueType == 'FILE'"
            v-slot="{ errors }"
            :name="configParameter.prettyName"
            rules="required"
          >
            <v-row>
              <v-textarea
                :key="textAreaFileContentComponentKey"
                v-model="configParameterValues[configParameter.name]"
                variant="outlined"
                density="compact"
                :error-messages="errors"

              />
            </v-row>
          </validation-provider>
          <validation-provider
              v-if="configParameter.valueType == 'KUBE_CONF'"
              v-slot="{ errors }"
              :name="configParameter.prettyName"
              rules="required"
          >
            <v-row>
              <v-textarea
                  :key="textAreaFileContentComponentKey"
                  v-model="configParameterValues[configParameter.name]"
                  variant="outlined"
                  density="compact"
                  :error-messages="errors"

              />
            </v-row>
          </validation-provider>
        </div>
      </v-container>

      <v-card-actions>
        <v-btn
          variant="text"
          @click="onBackButtonClicked"
        >
          Back
        </v-btn>
        <v-spacer />
        <v-btn
          variant="text"
          @click="onCancelButtonClicked"
        >
          Cancel
        </v-btn>
        <v-btn
          id="resource-create-button-create-cluster"
          variant="text"
          :color="invalid ? $vuetify.theme.disable : $vuetify.theme.themes.light.secondary"
          @click="invalid ? validate() : handleSubmit(onAddButtonClicked)"
        >
          Add
        </v-btn>
      </v-card-actions>
    </v-card>
  </validation-observer>
</template>

<script>
  import { mapGetters } from 'vuex'
  import clustersRestApi from '@/api/resource-management/clustersRestApi'
  import ResourcesCreateDialogPage from "@/components/resources/dialogs/create/ResourcesCreateDialogPage";
  import {ref} from "vue";

  const textAreaFileContentComponentKey = ref(0);

  export default {
    name: 'ResourcesCreateDialogPageAddExistingResourceCluster',
    enums: {
      ResourcesCreateDialogPage,
    },
    data () {
      return {
        selectedClusterType: '',
        configParameters: [],
        uploadedFiles: [],
        configParameterValues: {},
        textAreaFileContentComponentKey: 0
      }
    },
    mounted() {
      this.$emit('title-changed', 'Add existing cluster resource')
    },
    computed: {
      ...mapGetters(['availableClusterTypes']),
      availableClusterTypesWithSkipInstall () {
        let clusterTypes = []
        this.availableClusterTypes.forEach(clusterType => {
          if ('INSTALL' in clusterType.actions) {
            if (clusterType.actions['INSTALL'].skipable == true) {
              clusterTypes.push(clusterType)
            }
          }
        })
        return clusterTypes
      }
    },
    methods: {
      clearForm () {
        this.selectedClusterType = ''
        this.configParameters = []
        this.uploadedFiles = []
      },
      onSelectedClusterTypeChanged () {
        this.configParameters = this.selectedClusterType.actions['INSTALL'].configParameters
      },
      onFileChanged (configParameterName) {
        const reader = new FileReader()
        reader.readAsText(this.uploadedFiles[configParameterName])
        reader.onload = () => {
          this.configParameterValues[configParameterName] = reader.result
          this.textAreaFileContentComponentKey += 1;
        }
        console.log(this.configParameterValues)
      },
      onBackButtonClicked () {
        this.clearForm()
        this.$emit('page-changed', ResourcesCreateDialogPage.ADD_EXISTING_RESOURCE)
      },
      onCancelButtonClicked () {
        this.clearForm()
        this.$emit('canceled')
      },
      onAddButtonClicked () {
        console.log(this.configParameterValues)
        clustersRestApi.createClusterResource(this.selectedClusterType.id, true, {}, this.configParameterValues)
        this.clearForm()
        this.$emit('confirmed')
      },
    },
  }
</script>
