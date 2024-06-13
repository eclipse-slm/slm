<template>
  <ValidationForm
    ref="observer"
    v-slot="{ meta, handleSubmit, validate }"
  >
    <v-card>
      <v-container
        fluid
        class="pa-8"
      >
        <Field
          v-slot="{ field, errors }"
          v-model="selectedClusterType"
          name="Cluster Type"
          :rules="is_required"
        >
          <v-row id="resource-create-select-cluster-type">
            <v-icon
              size="large"
              class="mr-7 ml-2"
            >
              mdi-selection-multiple
            </v-icon>
            <v-select
              v-bind="field"
              :items="availableClusterTypesWithSkipInstall"
              item-title="name"
              label="Cluster Type"
              required
              return-object
              :error-messages="errors"
              :model-value="availableClusterTypesWithSkipInstall"
              @update:modelValue="onSelectedClusterTypeChanged"
            />
          </v-row>
        </Field>
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
          <Field
            v-if="configParameter.valueType == 'FILE'"
            v-slot="{ field, errors }"
            v-model="configParameterValues[configParameter.name]"
            :name="configParameter.prettyName"
            :rules="is_required"
          >
            <v-row>
              <v-textarea
                :key="textAreaFileContentComponentKey"
                v-bind="field"
                variant="outlined"
                density="compact"
                :error-messages="errors"
              />
            </v-row>
          </Field>
          <Field
            v-if="configParameter.valueType == 'KUBE_CONF'"
            v-slot="{ field, errors }"
            v-model="configParameterValues[configParameter.name]"
            :name="configParameter.prettyName"
            :rules="is_required"
          >
            <v-row>
              <v-textarea
                :key="textAreaFileContentComponentKey"
                v-bind="field"
                variant="outlined"
                density="compact"
                :error-messages="errors"
              />
            </v-row>
          </Field>
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
          :color="!meta.valid ? $vuetify.theme.disable : $vuetify.theme.themes.light.secondary"
          @click="!meta.valid ? validate() : handleSubmit(onAddButtonClicked)"
        >
          Add
        </v-btn>
      </v-card-actions>
    </v-card>
  </ValidationForm>
</template>

<script>
  import { mapGetters } from 'vuex'
  import clustersRestApi from '@/api/resource-management/clustersRestApi'
  import ResourcesCreateDialogPage from "@/components/resources/dialogs/create/ResourcesCreateDialogPage";
  import {ref} from "vue";
  import {Field, Form as ValidationForm } from "vee-validate";
  import * as yup from 'yup';

  const textAreaFileContentComponentKey = ref(0);

  export default {
    name: 'ResourcesCreateDialogPageAddExistingResourceCluster',
    components: {Field, ValidationForm },
    enums: {
      ResourcesCreateDialogPage,
    },
    setup(){
      const is_required = yup.string().required();
      return {
        is_required
      }
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
