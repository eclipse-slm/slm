<template>
  <v-card>
    <v-form>
      <v-container class="pa-8">
        <v-row>
          <v-icon
            size="large"
            class="mr-7 ml-2"
          >
            mdi-arrow-decision
          </v-icon>
          <v-select
            v-model="selectedClusterCreateOption"
            :items="clusterCreateOptions"
            label="Create Method"
            required
          />
        </v-row>

        <div v-if="selectedClusterCreateOption === 'useExistingResources'">
          <v-row id="resource-create-select-cluster-type">
            <v-icon
              size="large"
              class="mr-7 ml-2"
            >
              mdi-selection-multiple
            </v-icon>
            <v-select
              v-model="selectedClusterType"
              :items="availableClusterTypes"
              item-title="name"
              label="Cluster Type"
              required
              return-object
              @update:modelValue="onClusterTypeSelected"
            />
          </v-row>
          <div v-if="selectedClusterType">
            <v-list-subheader class="mt-6">
              Cluster Member:
            </v-list-subheader>
            <v-row
              v-for="clusterMemberType in selectedClusterType.clusterMemberTypes"
              :key="clusterMemberType.name"
              class="resource-create-row-cluster-type-option"
            >
              <v-col>
                <v-select
                  :key="clusterMemberType.id"
                  v-model="clusterMembersByMemberType[clusterMemberType.name]"
                  :label="clusterMemberType.prettyName"
                  :items="availableClusterResourcesByMemberType[clusterMemberType.name]"
                  item-title="hostname"
                  item-value="id"
                  return-object
                  hide-selected
                  closable-chips
                  chips
                  multiple
                  variant="outlined"
                  :error-messages="errorMessageByMemberType[clusterMemberType.name]"
                  @update:modelValue="limiter();onSelectedClusterMembersChanged($event)"
                >
                  <template slot="no-data">
                    No resources available
                  </template>
                </v-select>
              </v-col>
            </v-row>
          </div>
        </div>
      </v-container>
    </v-form>

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
        :disabled="!validInput"
        @click="onCreateButtonClicked"
      >
        Create
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>

import ClustersCreateDialogPage from "@/components/clusters/dialogs/ClustersCreateDialogPage";
import {useProviderStore} from "@/stores/providerStore";
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import {useResourceClustersStore} from "@/stores/resourceClustersStore";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";

export default {
    name: 'ClustersCreateDialogPageCreateNewCluster',
    enums: {
      ClustersCreateDialogPage,
    },
    setup(){
      const providerStore = useProviderStore();
      const resourceDevicesStore = useResourceDevicesStore();
      const resourceClustersStore = useResourceClustersStore();

      return {providerStore, resourceDevicesStore, resourceClustersStore};
    },
    data () {
      return {
        selectedClusterCreateOption: 'useExistingResources',
        selectedClusterType: '',
        availableClusterResourcesByMemberType: {},
        clusterMembersByMemberType: {},
        errorMessageByMemberType: {},
        minNumberByMemberType: {},
        validInput: false,
      }
    },
    computed: {
      virtualResourceProviders() {
        return this.providerStore.virtualResourceProviders
      },
      availableClusterTypes() {
        return this.resourceClustersStore.availableClusterTypes;
      },
      resources() {
        return this.resourceDevicesStore.resources;
      },

      clusterCreateOptions () {
        const options = [
          {
            title: 'Use existing resources',
            value: 'useExistingResources',
          },
        ]

        if (this.virtualResourceProviders.length !== 0) {
          options.push({
            title: 'Create Members',
            value: 'createVirtualResources',
          })
        }

        return options
      },
    },
    mounted() {
      this.$emit('title-changed', 'Create new cluster')
    },
    methods: {
      onClusterTypeSelected () {
        this.availableClusterResourcesByMember = {}

        this.selectedClusterType.clusterMemberTypes.forEach((memberType) => {
          this.availableClusterResourcesByMemberType[memberType.name] = this.resources
          this.minNumberByMemberType[memberType.name] = memberType.minNumber
          this.clusterMembersByMemberType[memberType.name] = []
          this.errorMessageByMemberType[memberType.name] = `At least ${memberType.minNumber} members are required`
        })
      },
      onSelectedClusterMembersChanged (selectedValues) {
        for (const availableClusterMemberType in this.availableClusterResourcesByMemberType) {
          this.availableClusterResourcesByMemberType[availableClusterMemberType] = this.resources
        }

        for (const clusterMemberType in this.clusterMembersByMemberType) {
          this.clusterMembersByMemberType[clusterMemberType].forEach(clusterMemberResource => {
            for (const availableClusterMemberType in this.availableClusterResourcesByMemberType) {
              if (availableClusterMemberType !== clusterMemberType) {
                this.availableClusterResourcesByMemberType[availableClusterMemberType] =
                  this.availableClusterResourcesByMemberType[availableClusterMemberType].filter(function (resource) {
                    return resource.id !== clusterMemberResource.id
                  })
              }
            }
          })
        }

      },
      limiter (e) {
        this.validInput = true
        for (const clusterMemberType in this.clusterMembersByMemberType) {
          const clusterMemberResourcesOfType = this.clusterMembersByMemberType[clusterMemberType]
          if (clusterMemberResourcesOfType.length < this.minNumberByMemberType[clusterMemberType]) {
            this.errorMessageByMemberType[clusterMemberType] = `At least ${this.minNumberByMemberType[clusterMemberType]} members are required`
            this.validInput = false
          } else {
            this.errorMessageByMemberType[clusterMemberType] = ''
          }
        }
      },

      clearForm () {
        this.selectedClusterCreateOption = 'useExistingResources'
        this.selectedClusterType = ''
        this.availableClusterResourcesByMemberType = {}
        this.clusterMembersByMemberType = {}
        this.errorMessageByMemberType = {}
        this.minNumberByMemberType = {}
        this.validInput = false
      },
      onBackButtonClicked () {
        this.clearForm()
        this.$emit('page-changed', ClustersCreateDialogPage.START)
      },
      onCancelButtonClicked () {
        this.clearForm()
        this.$emit('canceled')
      },
      onCreateButtonClicked () {
        const clusterMembers = {}
        for (const clusterMemberType in this.clusterMembersByMemberType) {
          this.clusterMembersByMemberType[clusterMemberType].forEach(clusterMember => {
            clusterMembers[clusterMember.id] = clusterMemberType
          })
        }

        ResourceManagementClient.clusterApi.createClusterResource({
          clusterTypeId: this.selectedClusterType.id,
          skipInstall: false,
          clusterMembers: clusterMembers,
          configParameterValues: {}
        }).then().catch(logRequestError)
        this.clearForm()
        this.$emit('confirmed')
      },
    },
  }
</script>
