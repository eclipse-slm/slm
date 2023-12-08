import ResourcesRestApi from '@/api/resource-management/resourcesRestApi.js'
import ClustersRestApi from '@/api/resource-management/clustersRestApi.js'
import LocationsRestApi from '@/api/resource-management/locationsRestApi.js'
import ProfilerRestApi from '@/api/resource-management/profilerRestApi.js'
import ApiState from '@/api/apiState'
import Vue from 'vue'
import AasRestApi from "@/api/resource-management/aasRestApi";

export default {
    state: {
        apiStateResources: ApiState.INIT,

        resources: [],
        aas: [],
        locations: [],
        profiler: [],
        resourceConnectionTypes: [],
        clusters: [],

        allDevices: {},

        selectedProject: 'fabos',
        searchResources: '',
        selectedResource: null,
        selectedResourceType: null,
        selectedResourceForDelete: null,
        selectedClusterForDelete: null,
        selectedClusterForScale: null,
        availableResourceTypes: [],
        availableDeploymentCapabilities: [],
        availableBaseConfigurationCapabilities: [],
        availableClusterTypes: [],
    },

    getters: {
        apiStateResources: (state) => {
            return state.apiStateResources
        },

        resources: (state) => {
            if (state.resources === undefined) {
                return []
            } else {
                return state.resources.filter(prop => {
                    return true
                })
            }
        },

        resourceAAS: (state) => {
          if (state.aas === undefined) {
              return []
          }   else {
              return state.aas
          }
        },

        profiler: (state) => {
            if (state.profiler === undefined) {
                return []
            } else {
                return state.profiler
            }
        },

        locations: (state) => {
            return state.locations
        },

        resourceConnectionTypes: (state) => {
            if (state.resourceConnectionTypes === undefined) {
                return []
            } else {
                return state.resourceConnectionTypes
            }
        },

        resourceById: (state) => (id) => {
            return state.resources.find(resource => resource.id === id)
        },

        nonClusterResources: (state) => {
            if (state.resources === undefined) {
                return []
            } else {
                return state.resources.filter(prop => {
                    return !prop.clusterMember
                })
            }
        },

        clusters: (state) => {
            return state.clusters
        },

        clusterById: (state) => (id) => {
            return state.clusters.find(cluster => cluster.id === id)
        },

        selectedResource: (state) => {
          return state.selectedResource
        },

        selectedResourceType: (state) => {
            return state.selectedResourceType
        },

        selectedResourceForDelete: (state) => {
            return state.selectedResourceForDelete
        },

        selectedClusterForDelete: (state) => {
            return state.selectedClusterForDelete
        },

        selectedClusterForScale: (state) => {
            return state.selectedClusterForScale
        },

        availableResourceTypes: (state) => {
            return state.availableResourceTypes
        },

        availableClusterTypes: (state) => {
            return state.availableClusterTypes
        },

        availableCapabilities: (state) => {
          return state.availableCapabilities
        },
        availableSingleHostCapabilities: (state) => {
          return state.availableCapabilities.filter(cap => {
              return cap.cluster === false
          })
        },
        availableSingleHostCapabilitiesNoDefault: (state) => {
            return state.availableCapabilities.filter(cap => {
                return cap.clusterMemberTypes === undefined
            })
        },
        availableBaseConfigurationCapabilities: (state) => {
            return state.availableCapabilities.filter(cap => {
                return cap.capabilityClass === "BaseConfigurationCapability"
            })
        },

        virtualMachines: (state) => {
            return state.resources.filter(function (resource) {
                return resource.resourceType === 'VirtualMachine'
            })
        },

        dockerHosts: (state) => {
            return state.resources.filter(function (resource) {
                return resource.resourceType === 'DockerHost'
            })
        },

        search: (state) => {
            return state.search
        },

        searchResources: (state) => {
          return state.searchResources
        },
    },

    actions: {
        async getResourcesFromBackend (context) {
            if (context.state.apiStateResources === ApiState.INIT) {
                context.commit('SET_API_STATE_RESOURCES', ApiState.LOADING)
            } else {
                context.commit('SET_API_STATE_RESOURCES', ApiState.UPDATING)
            }

            return await ResourcesRestApi.getResources(this.getters.selectedProject)
                .then(
                    resources => {
                        resources.forEach(resource => {
                            // MetricsRestApi.getMetricsOfResource(resource.id).then(metrics => {
                            //     resource.metrics = metrics
                            // })
                        })
                        context.commit('SET_RESOURCES', resources)
                        context.commit('SET_AVAILABLE_RESOURCE_TYPES', resources)
                        context.commit('SET_API_STATE_RESOURCES', ApiState.LOADED)
                    })
                .catch(e => {
                    console.debug(e)
                    context.commit('SET_RESOURCES', [])
                    context.commit('SET_AVAILABLE_RESOURCE_TYPES', [])
                    context.commit('SET_API_STATE_RESOURCES', ApiState.ERROR)
                })
        },

        async getResourceAASFromBackend (context) {
            if (context.state.apiStateResources === ApiState.INIT) {
                context.commit('SET_API_STATE_RESOURCES', ApiState.LOADING)
            } else {
                context.commit('SET_API_STATE_RESOURCES', ApiState.UPDATING)
            }

            return await AasRestApi.getResourceAAS().then(
              aas => {
                  context.commit('SET_RESOURCES_AAS', aas)
                  context.commit('SET_API_STATE_RESOURCES', ApiState.LOADED)
              }
            )
        },

        async getLocations(context) {
            return await  LocationsRestApi.getLocations()
              .then(
                locations => {
                    context.commit('SET_LOCATIONS', locations)
                }
              )
              .catch(e => {
                  console.debug(e)
                  context.commit('SET_LOCATIONS', [])
              })
        },

        async getProfiler(context) {
            return await  ProfilerRestApi.getProfiler()
              .then(
                profiler => {
                    context.commit('SET_PROFILER', profiler)
                }
              )
              .catch(e => {
                  console.debug(e)
                  context.commit('SET_PROFILER', [])
              })
        },

        async getResourceConnectionTypes(context) {
            return await ResourcesRestApi.getResourceConnectionTypes()
              .then(
                resourceConnectionTypes => {
                    context.commit('SET_RESOURCE_CONNECTION_TYPES', resourceConnectionTypes)
                }
              )
              .catch(e => {
                  console.debug(e)
                  context.commit('SET_RESOURCE_CONNECTION_TYPES', [])
              })
        },

        async getCluster (context) {
            return await ClustersRestApi.getCluster()
              .then(response => {
                  const clusters = []
                  for (var i in response) {
                      clusters.push({
                          id: response[i].id,
                          name: response[i].metaData['cluster_name'] ? response[i].metaData['cluster_name'] : response[i].name,
                          project: 'fabos',
                          clusterType: response[i].clusterType,
                          clusterMemberTypes: response[i].clusterMemberTypes,
                          nodeCount: response[i].nodes.length > 0? response[i].nodes.length: 'externally managed',
                          nodes: response[i].nodes,
                          memberMapping: response[i].memberMapping,
                          isManaged: response[i].managed,
                          metaData: response[i].metaData,
                          namespace: response[i].metaData['namespace'] ? response[i].metaData['namespace'] : '-',
                          username: response[i].metaData['cluster_user'] ? response[i].metaData['cluster_user'] : '-',
                          status: response[i].capabilityService.status
                      })
                  }
                context.commit('SET_CLUSTERS', clusters)
              })
        },

        async getClusterTypes (context) {
            return await ClustersRestApi.getClusterTypes()
              .then(response => {
                  context.commit('SET_AVAILABLE_CLUSTER_TYPES', response)
              })
        },

        async getDeploymentCapabilities (context) {
            return await ResourcesRestApi.getDeploymentCapabilities()
              .then(response => {
                  context.commit('SET_AVAILABLE_DEPLOYMENT_CAPABILITIES', response)
              })
        },

        updateResourcesStore (context) {
            // assure token gets refreshed
            Vue.prototype.$keycloak.keycloak.updateToken(1000000)
              .then(refreshed => {
                  context.dispatch('getCluster')
                  context.dispatch('getResourcesFromBackend')
                  context.dispatch('getResourceAASFromBackend')
                  context.dispatch('getResourceConnectionTypes')
                  context.dispatch('getVirtualResourceProviders')
                  context.dispatch('getServiceHosters')
                  context.dispatch('getProfiler')
              })
        },

        async setSelectedResource (context, resource) {
            if (resource.passwordType === 'otp') {
                const otp = await context.dispatch('getOtp', resource)
                resource.password = otp
            }

            context.commit('SET_SELECTED_RESOURCE', resource)
        },

        async getOtp (context, resource) {
            const project = context.getters.selectedProject
            const ip = resource.ip
            const username = 'vfk'

            return ResourcesRestApi.getOtp(project, ip, username)
        },

        async updateOtp (context) {
            const otp = await context.dispatch('getOtp', context.getters.selectedResource)

            context.commit('SET_OTP_OF_SELECTED_RESOURCE', otp)
        },
    },

    mutations: {
        SET_API_STATE_RESOURCES (state, apiState) {
            state.apiStateResources = apiState
        },
        SET_SELECTED_RESOURCE_TYPE (state, resourceType) {
            state.selectedResourceType = resourceType
        },
        SET_SELECTED_RESOURCE (state, resource) {
            state.selectedResource = resource
        },
        SET_OTP_OF_SELECTED_RESOURCE (state, otp) {
            state.selectedResource.password = otp
        },
        SET_RESOURCES (state, resources) {
            if (state.resources !== undefined) {
                const markedForDeleteResources = state.resources.filter((res) => res.markedForDelete === true)

                // Assure markedForDelete Property is not overwritten by polling
                for (const markedForDeleteResource of markedForDeleteResources) {
                    const resourceToUpdate = resources.find((res) => res.id === markedForDeleteResource.id)
                    if (resourceToUpdate !== undefined) {
                        resourceToUpdate.markedForDelete = true
                    }
                }
                state.resources = resources
            }
        },
        SET_RESOURCES_AAS (state, aas) {
            state.aas = aas
        },
        SET_LOCATIONS(state, locations) {
          state.locations = locations
        },
        SET_PROFILER(state, profiler) {
            state.profiler = profiler
        },
        SET_RESOURCE_CONNECTION_TYPES (state, resourceConnectionTypes) {
          state.resourceConnectionTypes = resourceConnectionTypes
        },
        SET_CLUSTERS (state, clusters) {
            if (clusters !== undefined) {
                state.clusters = clusters
            } else {
                state.clusters = []
            }
        },
        SET_AVAILABLE_RESOURCE_TYPES (state, resources) {
            const availableResourceTypes = []

            resources.forEach(item => {
                if (!(item.resourceType in availableResourceTypes)) {
                    availableResourceTypes.push(item.resourceType)
                }
            })

            state.availableResourceTypes = availableResourceTypes
        },
        SET_AVAILABLE_CLUSTER_TYPES (state, types) {
            if (types === undefined) {
                state.availableClusterTypes = []
            } else {
                state.availableClusterTypes = types
            }
        },
        SET_AVAILABLE_DEPLOYMENT_CAPABILITIES (state, capabilities) {
            if (capabilities === undefined) {
                state.availableCapabilities = []
            } else {
                // Sort alphabetically
                state.availableCapabilities = capabilities.sort((a, b) => {
                    return a.name.localeCompare(b.name)
                })
            }
        },
        SET_SELECTED_ClUSTER_FOR_DELETE (state, cluster) {
            state.selectedClusterForDelete = cluster
        },
        SET_SELECTED_ClUSTER_FOR_SCALE (state, cluster) {
            state.selectedClusterForScale = cluster
        },
        SET_RESOURCE_MARKED_FOR_DELETE (state, resource) {
            const filteredRes = state.resources.find(obj => obj.id === resource.id)
            filteredRes.markedForDelete = true
        },
        SET_SEARCH_RESOURCES (state, value) {
          state.searchResources = value
        },
    },

}
