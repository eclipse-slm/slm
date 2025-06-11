<template>
  <div>
    <v-card>
      <v-data-table
        :headers="clusterHeaders"
        :items="clusters"
        :row-props="rowClass"
        :single-expand="true"
        :expanded="expanded"
        show-expand
        @click:row="expandRow"
      >
        <template
          #item.actions="{ item }"
        >
          <div class="text-right">
            <v-btn
              color="info"
              :disabled="item.isManaged"
              @click.stop="addNodeToCluster(item)"
            >
              <v-icon icon="mdi-server-plus" />
            </v-btn>
            <v-btn
              class="ml-4"
              color="accent"
              :disabled="item.isManaged"
              @click.stop="removeNodeFromCluster(item)"
            >
              <v-icon
                color="white"
                icon="mdi-server-minus"
              />
            </v-btn>
            <v-btn
              class="ml-4"
              color="error"
              @click.stop="deleteCluster(item)"
            >
              <v-icon icon="mdi-delete" />
            </v-btn>
          </div>
        </template>

        <template #expanded-row="{ columns, item}">
          <td :colspan="columns.length">
            <v-table
              dense
              class="my-5"
            >
              <template #default>
                <thead>
                  <tr>
                    <th>Node Name</th>
                    <th>IP</th>
                    <th>ID</th>
                    <th>Member Type</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="node in item.nodes"
                    :key="node.node"
                  >
                    <td>{{ node.Node }}</td>
                    <td>{{ node.Address }}</td>
                    <td>{{ node.ID }}</td>
                    <td>
                      <v-chip
                        class="mx-1"
                        size="small"
                        value="test"
                      >
                        {{ getClusterMemberTypeByNodeId(item.memberMapping[node.ID], item.clusterMemberTypes) }}
                      </v-chip>
                    </td>
                  </tr>
                </tbody>
              </template>
            </v-table>
          </td>
        </template>
      </v-data-table>
    </v-card>

    <cluster-delete-dialog
      :show-dialog="showClusterDeleteDialog"
      :cluster="clusterForDelete"
      @canceled="showClusterDeleteDialog = false"
      @confirmed="showClusterDeleteDialog = false"
    />
    <cluster-scale-dialog :action="scaleAction" />
  </div>
</template>

<script>

import ClusterScaleDialog from '@/components/resources/dialogs/ClusterScaleDialog.vue'
import ClusterDeleteDialog from '@/components/resources/dialogs/ClusterDeleteDialog.vue'
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";

export default {
    name: 'ResourcesTableClusters',
    components: {
      ClusterScaleDialog,
      ClusterDeleteDialog,
    },
    setup(){
      const resourceDevicesStore = useResourceDevicesStore();
      return {resourceDevicesStore};
    },
    data () {
      return {
        expanded: [],
        clusterHeaders: [
          { title: '', key: 'data-table-expand' },
          { title: 'ID', key: 'id' },
          { title: 'Name', key: 'name' },
          // { text: 'Server', value: 'server' },
          { title: 'User', key: 'username' },
          { title: 'Namespace', key: 'namespace' },
          { title: 'Status', key: 'status'},
          { title: 'Type', key: 'clusterType' },
          { title: 'Node Count', key: 'nodeCount' },
          { value: 'actions' },
        ],
        nodeHeaders: [
          { title: 'Node Name', value: 'node' },
        ],
        scaleAction: null,
        clusterForDelete: null,
        showClusterDeleteDialog: false,
      }
    },
    computed: {
      clusters () {
        return this.resourceDevicesStore.clusters
      },
    },
    methods: {
      addNodeToCluster (cluster) {
        this.scaleAction = 'up'
        this.resourceDevicesStore.selectedClusterForScale_ = cluster;
      },
      removeNodeFromCluster (cluster) {
        this.scaleAction = 'down'
        this.resourceDevicesStore.selectedClusterForScale_ = cluster;
      },
      selectClusterForDelete (cluster) {
        this.resourceDevicesStore.selectedClusterForDelete_ = cluster;
      },
      expandRow (event, row) {
        const item = row.item;
        const found = this.expanded.find(i => {
          return i.name === item.name
        })

        if (this.expanded.length > 0) {
          this.expanded = []
        }

        if (!found && item.nodes.length > 0){
          this.expanded.push(item)
        }
      },
      rowClass ({item}) {
        return {
          class: {
            'text-grey text--lighten-1 row-pointer': item.markedForDelete,
            'row-pointer': item.markedForDelete
          }
        }
      },
      getClusterMemberTypeByNodeId (clusterMemberTypeName, clusterMemberTypes) {
        return clusterMemberTypes.find((clusterMemberType) => {
          return clusterMemberType.name === clusterMemberTypeName
        })?.prettyName
      },
      deleteCluster (clusterToDelete) {
        this.clusterForDelete = clusterToDelete
        this.showClusterDeleteDialog = true
      },
    },
  }

</script>

<style scoped>
.row-pointer  {
  cursor: pointer;
}
</style>
