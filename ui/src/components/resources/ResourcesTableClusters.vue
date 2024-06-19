<template>
  <div>
    <v-card>
      <v-data-table
        :headers="clusterHeaders"
        :items="clusters"
        :row-props="rowClass"
        :single-expand="true"
        :expanded.sync="expanded"
        show-expand
        @click:row="expandRow"
      >
        <template #top>
          <v-toolbar flat>
            <v-toolbar-title>
              Clusters
            </v-toolbar-title>
          </v-toolbar>
        </template>
        <template
          #item.actions="{item}"
        >
          <div class="text-right">
            <v-btn
              color="info"
              :disabled="item.isManaged"
              icon="mdi-server-plus"
              @click.stop="addNodeToCluster(item)"
            />
            <v-btn
              class="ml-4"
              color="warning"
              :disabled="item.isManaged"
              icon="mdi-server-minus"
              @click.stop="removeNodeFromCluster(item)"
            />
            <v-btn
              class="ml-4"
              color="error"
              icon="mdi-delete"
              @click.stop="deleteCluster(item)"
            />
          </div>
        </template>

        <template #item.data-table-expand="{ item, isExpanded }">
          <td
            v-if="item.nodes.length > 0"
            class="text-start"
          >
            <v-btn
              variant="text"
              :icon="isExpanded ? 'mdi-close' : 'mdi-chevron-down'"
              :class="{'v-data-table__expand-icon--active' : isExpanded}"
            />
          </td>
        </template>

        <template #expanded-item="{headers, item}">
          <td :colspan="headers.length">
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
  import { mapGetters } from 'vuex'
  import ClusterScaleDialog from '@/components/resources/dialogs/ClusterScaleDialog'
  import ClusterDeleteDialog from '@/components/resources/dialogs/ClusterDeleteDialog'

  export default {
    name: 'ResourcesTableClusters',
    components: {
      ClusterScaleDialog,
      ClusterDeleteDialog,
    },
    data () {
      return {
        expanded: [],
        clusterHeaders: [
          { text: '', value: 'data-table-expand' },
          { text: 'ID', value: 'id' },
          { text: 'Name', value: 'name' },
          // { text: 'Server', value: 'server' },
          { text: 'User', value: 'username' },
          { text: 'Namespace', value: 'namespace' },
          { text: 'Status', value: 'status'},
          { text: 'Type', value: 'clusterType' },
          { text: 'Node Count', value: 'nodeCount' },
          { value: 'actions' },
        ],
        nodeHeaders: [
          { text: 'Node Name', value: 'node' },
        ],
        scaleAction: null,
        clusterForDelete: null,
        showClusterDeleteDialog: false,
      }
    },
    computed: {
      ...mapGetters(['clusters']),
    },
    methods: {
      addNodeToCluster (cluster) {
        this.scaleAction = 'up'
        this.$store.commit('SET_SELECTED_ClUSTER_FOR_SCALE', cluster)
      },
      removeNodeFromCluster (cluster) {
        this.scaleAction = 'down'
        this.$store.commit('SET_SELECTED_ClUSTER_FOR_SCALE', cluster)
      },
      selectClusterForDelete (cluster) {
        this.$store.commit('SET_SELECTED_ClUSTER_FOR_DELETE', cluster)
      },
      expandRow (row) {
        const found = this.expanded.find(i => {
          return i.name === row.name
        })

        if (this.expanded.length > 0) {
          this.expanded = []
        }

        if (!found && row.nodes.length > 0){
          this.expanded.push(row)
        }
      },
      rowClass (item) {
        return item.markedForDelete ? 'text-grey text--lighten-1 row-pointer' : 'row-pointer'
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
