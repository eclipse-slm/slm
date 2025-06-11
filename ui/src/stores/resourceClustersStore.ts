import ApiState from '@/api/apiState'
import {defineStore} from "pinia";
import {useProviderStore} from "@/stores/providerStore";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";

interface ResourcesClustersStoreState{
    apiState: number,

    clusters: any[],
    selectedClusterForDelete: any,
    selectedClusterForScale: any,
    availableClusterTypes: any[],
}

export const useResourceClustersStore = defineStore('resourcesClustersStore', {
    persist: true,

    state:():ResourcesClustersStoreState => ({
        apiState: ApiState.INIT,

        clusters: [],

        selectedClusterForDelete: null,
        selectedClusterForScale: null,
        availableClusterTypes: [],
    }),

    getters: {
        clusterById: (state) => (id) => {
            return state.clusters.find(cluster => cluster.id === id)
        },
    },

    actions: {
        async getCluster () {
            try{
                const response = await ResourceManagementClient.clusterApi.getClusterResources();
                const receivedClusters: any[] = []
                if (response.data && response.data.length > 0) {
                    for (const i in response.data) {
                        receivedClusters.push({
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

                    this.clusters = receivedClusters
                }
            } catch (e) {
                logRequestError(e)
                return [];
            }

        },

        async getClusterTypes () {
            return await ResourceManagementClient.clusterApi.getClusterTypes()
                .then(response => {
                    if(response.data && response.data.length > 0) {
                        const receivedClusterTypes = response.data;
                        if (receivedClusterTypes === undefined) {
                            this.availableClusterTypes = []
                        } else {
                            this.availableClusterTypes = receivedClusterTypes
                        }
                    }
                }).catch(logRequestError)
        },

        async updateStore () {
            const providerStore = useProviderStore();

            return Promise.all([
                this.getCluster(),
                this.getClusterTypes()
            ]).then(() => {
                this.apiState = ApiState.LOADED;
                console.log("resourceClustersStore updated")
            }).catch((e) => {
                this.apiState = ApiState.ERROR;
                console.log("Failed to update resourceClustersStore: ", e)
            });
        },
    },
});

