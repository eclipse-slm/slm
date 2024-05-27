<template>
  <v-card>
    <v-data-table
      id="resource-table-single-host"
      :headers="tableHeaders"
      :items="filteredResources"
      :search="searchResources"
      :sort-by.sync="sortBy"
      :row-props="rowClass"
      item-key="id"

      @click:row="setSelectedResource"
    >
      <template #top>
        <v-toolbar flat>
          <v-spacer></v-spacer>
          <v-toolbar-title>
            <v-tooltip start close-delay="2000">
              <template #activator="{ props }">
                <v-btn
                    v-if="areProfilerAvailable"
                    color="primary"
                    @click="runProfiler"
                    v-bind="props"
                >
                  <v-icon color="white">
                    mdi-magnify
                  </v-icon>
                </v-btn>
              </template>
              <span>Runs all <a href="https://eclipse-slm.github.io/slm/docs/usage/profiler/">profiler</a> on all devices</span>
            </v-tooltip>
          </v-toolbar-title>
        </v-toolbar>
        <v-toolbar-items
          v-if="resourcesHaveLocations()"
          class="ml-8 mb-8"
        >
          <div class="mr-10">
            <v-btn-toggle
              v-model="groupBy"
              mandatory
            >
              <v-btn
                size="small"
                :model-value="null"
                :color="groupBy == null ? 'secondary' : 'disabled'"
                style="height:40px"
              >
                <v-icon>mdi-ungroup</v-icon>
              </v-btn>
              <v-btn
                size="small"
                model-value="location.name"
                :color="groupBy == 'location.name' ? 'secondary' : 'disabled'"
                style="height:40px"
              >
                <v-icon>mdi-group</v-icon>
              </v-btn>
            </v-btn-toggle>
          </div>
          <div class="mr-10">
            <v-select
              v-model="filterResourcesByLocations"
              :items="locations"
              item-title="name"
              item-value="id"
              label="filter by location"
              density="compact"
              variant="outlined"
              hide-details
              closable-chips
              multiple
              clearable
              @update:modelValue="filterResources"
            />
          </div>
        </v-toolbar-items>
      </template>

      <template #group.header="{items, isOpen, toggle}">
        <th
          :colspan="tableHeaders.length"
        >
          <v-icon @click="toggle">
            {{ isOpen ? 'mdi-minus' : 'mdi-plus' }}
          </v-icon>
          {{ getLocationNameForGroupHeader(items) }}
        </th>
      </template>

      <template #item.capabilityServices="{ item }">
        <v-tooltip
          v-for="capabilityService in getDeploymentCapabilityServices(item.capabilityServices)"
          :key="capabilityService.capability.name"
          location="top"
        >
          <template
              #activator="{ props }"
          >
            <v-chip
              v-bind="props"
              :key="capabilityService.capability.name"
              class="ma-1"
            >
              <v-icon start>
                {{ getChipIconByCapabilityService(capabilityService) }}
              </v-icon>
              {{ capabilityService.capability.name }}
            </v-chip>
          </template>
          <span>Status: {{ capabilityService.status }}</span>
        </v-tooltip>
      </template>

      <template #item.fabosDevice="{ item }">
        <v-tooltip
          v-if="hasBaseConfigurationCapabilityService(item.capabilityServices)"
          location="right"
        >
          <template
              #activator="{ props }"
          >
            <v-icon
              :color="getFabOSDeviceIcon(item.capabilityServices).color"
              v-bind="props"
            >
              {{ getFabOSDeviceIcon(item.capabilityServices).logo }}
            </v-icon>
          </template>
          <span>Status: {{ getStatusOfBaseConfigurationCapabilityService(item.capabilityServices) }}</span>
        </v-tooltip>
        <v-icon
          v-else
          color="primary"
        >
          mdi-close-circle-outline
        </v-icon>
      </template>

      <template #item.cpuArch="{ item }">
        <div v-if="item.metrics != null">
          {{ item.metrics.cpuArch }}
        </div>
        <div v-else>
          -
        </div>
      </template>

      <template #item.cpuCores="{ item }">
        <div v-if="item.metrics != null">
          {{ item.metrics.cpuCores }}
        </div>
        <div v-else>
          -
        </div>
      </template>

      <template #item.os="{ item }">
        <v-icon>{{ getOsIcon(item.remoteAccessService) }}</v-icon>
      </template>

      <template
        #item.actions="{ item }"
      >
        <div
          v-if="!item.markedForDelete"
          class="text-right btn-col"
        >
          <v-menu>
            <template #activator="{ props }">
              <v-btn
                id="mushroom-button"
                color="info"
                v-bind="props"
                :disabled="item.clusterMember || availableSingleHostCapabilitiesNoDefault.length == 0"
                class="resource-single-host-add-capability"
              >
                <v-icon>
                  mdi-mushroom-outline
                </v-icon>
              </v-btn>
            </template>
            <v-list
              v-for="capabilityClass in getUniqueCapabilityClasses"
              :key="capabilityClass"
              class="resources-capability-menu"
            >
              <h6 class="ml-4">
                {{ insertWhiteSpaceInCamelCase(capabilityClass) }}
              </h6>
              <v-list-item
                v-for="capability in getCapabilitiesByCapabilityClass(capabilityClass)"
                :key="capability.id"
              >
                <v-btn-toggle>
                  <!-- Capability Install Button -->
                  <v-btn
                    v-if="showCapabilityInstallButton(item, capability)"
                    id="install-button"
                    :disabled="!resourceHasRemoteAccessService(item)"
                    color="info"
                    style="height:36px"
                    @click="openDefineCapabilityParamsDialog(item.id, capability.id, false)"
                  >
                    <v-icon
                      start
                      color="white"
                    >
                      {{ capability.logo }}
                    </v-icon>
                    {{ capability.name }}
                  </v-btn>
                  <!-- Capability Skip Install Button -->
                  <v-btn
                    v-if="showCapabilitySkipInstallButton(item,capability)"
                    color="info"
                    style="height:36px"
                    @click="openDefineCapabilityParamsDialog(item.id, capability.id, true)"
                  >
                    <v-icon color="white">
                      mdi-skip-next
                    </v-icon>
                  </v-btn>
                  <!-- Capability Uninstall Button -->
                  <v-btn
                    v-if="isCapabilityInstalledOnResource(item, capability)"
                    color="error"
                    style="height:36px"
                    @click="removeCapability(item.id, capability.id)"
                  >
                    <v-icon
                      start
                      color="white"
                    >
                      {{ capability.logo }}
                    </v-icon>
                    {{ capability.name }}
                  </v-btn>
                </v-btn-toggle>
              </v-list-item>
            </v-list>
          </v-menu>
          <v-btn
            :disabled="item.clusterMember"
            class="ml-4 resource-single-host-delete"
            color="error"
            @click.stop="resourceToDelete = item"
          >
            <v-icon>mdi-delete</v-icon>
          </v-btn>
        </div>
      </template>
    </v-data-table>
    <confirm-dialog
      :show="resourceToDelete != null"
      :title="'Delete resource ' + (resourceToDelete == null ? '' : resourceToDelete.hostname)"
      text="Do you really want to delete this resource?"
      @confirmed="deleteResource(resourceToDelete)"
      @canceled="resourceToDelete = null"
    />
    <capability-params-dialog
      :show-dialog="showCapabilityParamsDialog"
      :capability-id="selectedCapabilityId"
      :resource-id="selectedResourceId"
      :skip-install="selectedSkipInstall"
      @install="runAddCapability"
      @canceled="unsetSelected()"
    />
  </v-card>
</template>

<script>
  import {
    mapGetters,
  } from 'vuex'
  import ConfirmDialog from '@/components/base/ConfirmDialog'
  import CapabilityParamsDialog from "@/components/resources/dialogs/CapabilityParamsDialog.vue";
  import ResourcesRestApi from '@/api/resource-management/resourcesRestApi'
  import { capabilityUtilsMixin } from '@/utils/capabilityUtils'
  import ProfilerRestApi from "@/api/resource-management/profilerRestApi";
  import Vue from "vue";
  import {app} from "@/main";

  export default {
    name: 'ResourcesTableSingleHosts',
    components: {CapabilityParamsDialog, ConfirmDialog },
    data () {
      return {
        tableHeaders: [
          { title: 'Capabilities', value: 'capabilityServices' },
          { title: 'Hostname', value: 'hostname' },
          { title: 'IP', value: 'ip' },
          { title: 'Location', value: 'location.name'},
          { title: 'FabOS Device', value: "fabosDevice" },
          { title: 'Connection Type', value:'remoteAccessService.connectionType'},
          { title: 'Port', value:'remoteAccessService.Port'},
          // { text: 'CPU Arch', value: 'cpuArch' },
          // { text: 'CPU Cores', value: 'cpuCores' },
          { title: 'OS', value: 'os' },
          { title: 'UUID', value: 'id', sortable: false },
          { title: 'Cluster Member', value: 'clusterMember' },
          { title:'test', value: 'actions', sortable: false },
        ],
        groupBy: [],
        sortBy: [{key: 'ip', order: 'asc'}],
        sortDesc: false,
        showLvlUpMenu: false,
        showResourcesDeleteDialog: false,
        resourceToDelete: null,
        resourceMetrics: {},
        selectedResourceId: null,
        selectedCapabilityId: null,
        selectedSkipInstall: null,
        selectedParamMap: null,
        filterResourcesByLocations: [],
        filteredResources: []
      }
    },
    computed: {
      ...mapGetters([
        'resources',
        'locations',
        'profiler',
        'searchResources',
        'selectedResourceForDelete',
        'availableSingleHostCapabilitiesNoDefault',
      ]),
      areProfilerAvailable() {
        if(this.profiler.length > 0)
          return true
        else
          return false
      },
      showCapabilityParamsDialog() {
        if(this.selectedResourceId !== null && this.selectedCapabilityId !== null && this.selectedSkipInstall !== null)
          return true
        else
          return false
      },
      getUniqueCapabilityClasses() {
        return [...new Set(
          this.availableSingleHostCapabilitiesNoDefault.map(shc => shc.capabilityClass)
        )].sort()
      }
    },
    watch: {
      resources() {
        this.filterResources()
      }
    },
    created() {
      this.filteredResources = this.resources
    },
    mixins: [capabilityUtilsMixin],
    methods: {
      hasBaseConfigurationCapabilityService(capabilityServicesOfResource) {
        return capabilityServicesOfResource.some(cs => cs.capability.capabilityClass=="BaseConfigurationCapability")
      },
      getStatusOfBaseConfigurationCapabilityService(capabilityServicesOfResource) {
        const bcCapabilityService = capabilityServicesOfResource.find(cs => cs.capability.capabilityClass=="BaseConfigurationCapability")

        return bcCapabilityService.status
      },
      getDeploymentCapabilityServices(capabilityServices) {
        return capabilityServices.filter(cs => cs.capability.capabilityClass!="BaseConfigurationCapability")
      },
      deleteResource (resource) {
        const resourceId = resource.id
        ResourcesRestApi.deleteResource(resourceId).then(response => {
        })
        this.$store.commit('SET_RESOURCE_MARKED_FOR_DELETE', resource)
        this.resourceToDelete = null
      },
      openDefineCapabilityParamsDialog(resourceId, capabilityId, skipInstall) {
        if(this.isDefineCapabilityDialogRequired(capabilityId, skipInstall) == false) {
          this.addCapability(resourceId,capabilityId,skipInstall,{})
          return;
        }

        this.selectedResourceId = resourceId
        this.selectedCapabilityId = capabilityId
        this.selectedSkipInstall = skipInstall
      },
      runAddCapability(configParameter) {
        this.addCapability(this.selectedResourceId, this.selectedCapabilityId, this.selectedSkipInstall, configParameter)

      },
      addCapability (resourceId, capabilityId, skipInstall, configParameterMap) {
        ResourcesRestApi.addCapabilityToSingleHost(resourceId, capabilityId, skipInstall, configParameterMap)
        this.unsetSelected()
      },
      unsetSelected() {
        this.selectedResourceId = null
        this.selectedCapabilityId = null
        this.selectedSkipInstall = null
        this.selectedParamMap = null
      },
      hasCapabilityConfigParamsWithRequiredTypeAlways(capabilityId) {
        if(this.getCapability(capabilityId).actions["INSTALL"].configParameters === undefined)
          return false

        return this.getCapability(capabilityId).actions["INSTALL"].configParameters.filter(
          param => param.requiredType === "ALWAYS"
        ).length != 0
      },
      isDefineCapabilityDialogRequired(capabilityId, skipInstall) {
        //false
        if(this.getParamsOfInstallAction(capabilityId).length == 0)
          return false
        else if (this.hasCapabilityConfigParamsWithRequiredTypeAlways(capabilityId) == false && skipInstall == true)
          return false
        else
          return true
      },
      removeCapability (resourceId, capabilityId) {
        ResourcesRestApi.removeCapabilityFromSingleHost(resourceId, capabilityId)
      },
      setSelectedResource (event, { item }) {
        this.$emit('resource-selected', item)
      },
      rowClass (resource) {
        return {
          class: {
            'text-grey text--lighten-1 row-pointer': resource.markedForDelete,
            'row-pointer': resource.markedForDelete
          }
        }
      },
      isCapabilityInstalledOnResource (resource, capability) {
        if(resource.capabilityServices != null)
          return resource.capabilityServices.filter(capService => capService.capability.name === capability.name).length > 0
        else
          return false
      },
      isCapabilitySkipable(capability) {
        const installAction = capability.actions["INSTALL"]
        if(installAction !== undefined && installAction.skipable !== undefined)
          return installAction.skipable
        else
          return false
      },
      showCapabilityInstallButton(resource, capability) {
        return !this.isCapabilityInstalledOnResource(resource,capability)
      },
      showCapabilitySkipInstallButton(resource, capability) {
        return !this.isCapabilityInstalledOnResource(resource,capability) && this.isCapabilitySkipable(capability)
      },
      showCapabilityUninstallButton(resource, capability) {
        return this.isCapabilityInstalledOnResource(resource,capability)
      },
      getChipIconByCapabilityService(capabilityService) {
        const status = capabilityService.status

        if(status === "READY")
          return "mdi-check"

        if(status === "INSTALL")
          return "mdi-plus"

        if(status === "UNINSTALL")
          return "mdi-minus"

        if(status === "UNKNOWN")
          return "mdi-help"

        if(status === "FAILED")
          return "mdi-alert-circle-outline"

      },
      getOsIcon(remoteAccessService) {
        let conType
        try {
          conType = remoteAccessService.connectionType
        } catch(e) {
          conType = null
        }


        if(conType === "ssh")
          return "mdi-penguin"
        if(conType === "WinRM" || conType === "WinSsh")
          return "mdi-microsoft-windows"
        else
          return "mdi-help-circle-outline"
      },
      getLocationNameForGroupHeader(items) {
        if(items[0].location === null)
          return "no location"

        return items[0].location.name
      },
      resourcesHaveLocations() {
        const locationNames = [...new Set(
          this.resources.map(r => {
            if(r.location == null)
              return ''
            else
              return r.location.name
          })
        )]
        const hasLocations = locationNames.length > 1

        if(!hasLocations)
          this.groupBy = null

        return hasLocations
      },
      filterResources() {
        if(this.filterResourcesByLocations.length == 0) {
          this.filteredResources = this.resources
          return
        }

        this.filteredResources = this.resources.filter(r => {
          if(r.location == null)
            return false

          return this.filterResourcesByLocations.includes( r.location.id )
        })
      },
      resourceHasRemoteAccessService(resource) {
        return resource.remoteAccessService != null
      },
      getFabOSDeviceIcon(capabilityServices) {
        const baseConfigService = capabilityServices.find(cs => cs.capability.capabilityClass === "BaseConfigurationCapability")

        if(baseConfigService.status == "INSTALL" || baseConfigService.status == "UNINSTALL")
          return {
            "color": "info",
            "logo": "mdi-timer-sand"
          }
        if(baseConfigService.status == "READY")
          return {
            "color": "info",
            "logo": "mdi-check-circle-outline"
          }
        if(baseConfigService.status == "FAILED")
          return {
            "color": "error",
            "logo": "mdi-message-alert"
          }
        else
          return {
            "color": "warn",
            "logo": "mdi-help-circle-outline"
          }
      },
      getCapabilitiesByCapabilityClass(capabilityClass) {
        return this.availableSingleHostCapabilitiesNoDefault.filter(shc => shc.capabilityClass === capabilityClass)
      },
      insertWhiteSpaceInCamelCase(string) {
        return string.replace(/([A-Z])/g, ' $1').trim()
      },
      runProfiler() {
        let result = ProfilerRestApi.runProfiler()
        app.config.globalProperties.$toast.info('Started Profiler for all devices.')
      }
    }
  }

</script>

<style scoped>
.row-pointer  {
  cursor: pointer;
}

.v-tooltip__content {
  pointer-events: initial;
}
</style>
