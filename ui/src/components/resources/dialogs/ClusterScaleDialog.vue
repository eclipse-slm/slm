<template>
  <v-dialog
    v-model="showDialog"
    width="600"
    @click:outside="closeDialog"
  >
    <template v-slot:default="{}">
      <v-card v-if="page=='scale-virtual'">
        <v-toolbar
          color="primary"
          theme="dark"
        >
          Scale Resource  <strong>{{ selectedClusterForScale.name }}</strong>
        </v-toolbar>
        <v-card-text>
          Do your really want to scale this resource {{ action }}?
        </v-card-text>
        <v-card-actions>
          <v-btn
            variant="text"
            @click="page=''"
          >
            Back
          </v-btn>
          <v-spacer />
          <v-btn
            variant="text"
            @click="closeDialog"
          >
            Submit
          </v-btn>
        </v-card-actions>
      </v-card>
      <v-card v-else-if="page=='scale-bare-metal'">
        <v-toolbar
          color="primary"
          theme="dark"
        >
          Select Bare Metal Resource
        </v-toolbar>
        <v-container>
          <!--          <p>-->
          <!--            Select resource which should be added to cluster <strong class="text-no-wrap">{{ selectedClusterForScale.name }}</strong>-->
          <!--          </p>-->
          <v-select
            v-model="selectedBareMetalResource"
            label="Select Resource"
            :items="nonClusterResources"
            item-title="hostname"
            item-value="id"
          />
        </v-container>
        <v-card-actions>
          <v-btn
            variant="text"
            @click="page=''"
          >
            Back
          </v-btn>
          <v-spacer />
          <v-btn
            variant="text"
            @click="scaleCluster"
          >
            Submit
          </v-btn>
        </v-card-actions>
      </v-card>
      <v-card v-else>
        <v-container
          v-if="action=='up'"
          class="ma-0 pa-0"
        >
          <v-toolbar
            color="primary"
            theme="dark"
          >
            Add Bare Metal Machine / Create Virtual Machine
          </v-toolbar>
          <v-container>
            <v-row class="my-3">
              <v-col class="text-center">
                <v-btn
                  color="secondary"
                  size="x-large"
                  tile
                  @click="page='scale-bare-metal'"
                >
                  <v-icon
                    class="mr-5 ml-2"
                    size="x-large"
                    start
                  >
                    mdi-plus
                  </v-icon>
                  Add
                </v-btn>
              </v-col>
              <v-col class="text-center">
                <v-btn
                  color="secondary"
                  size="x-large"
                  tile
                  @click="page='scale-virtual'"
                >
                  <v-icon
                    class="mr-5 ml-2"
                    size="large"
                    start
                  >
                    mdi-pencil-outline
                  </v-icon>
                  Create
                </v-btn>
              </v-col>
            </v-row>
          </v-container>
        </v-container>
        <v-container
          v-else-if="action=='down'"
          class="ma-0 pa-0"
        >
          <v-toolbar
            color="primary"
            theme="dark"
          >
            Select resource to be removed from cluster
          </v-toolbar>

          <v-container>
            <v-select
              v-model="selectedClusterMemberTypeName"
              :items="availableClusterMemberTypes"
              item-title="prettyName"
              label="Cluster Member Type"
            />
            <v-select
              v-model="selectedBareMetalResource"
              :items="downScalableClusterMembers"
              item-title="Node"
              item-value="ID"
              label="Node to be removed from Cluster"
            />
          </v-container>
          <v-card-actions>
            <v-btn
              variant="text"
              @click="closeDialog"
            >
              Close
            </v-btn>
            <v-spacer />
            <v-btn
              variant="text"
              @click="scaleCluster"
            >
              Submit
            </v-btn>
          </v-card-actions>
        </v-container>
      </v-card>
    </template>
  </v-dialog>
</template>

<script>
  import { mapGetters } from 'vuex'
  import ClustersRestApi from '@/api/resource-management/clustersRestApi.js'

  export default {
    name: 'ClusterScaleDialog',
    props: ['action'],
    data () {
      return {
        page: '',
        selectedBareMetalResource: '',
        selectedClusterMemberTypeName: '',
      }
    },
    computed: {
      ...mapGetters([
        'nonClusterResources',
        'selectedClusterForScale',
        'selectedProject',
        'availableClusterTypes',
      ]),
      showDialog () {
        return this.$store.getters.selectedClusterForScale !== null
      },
      downScalableClusterMembers () {

        if (this.selectedClusterForScale == null || this.selectedClusterMemberTypeName === '') {
          return []
        } else {
          const selectedClusterMemberType = this.availableClusterMemberTypes.find(member => { return member.prettyName === this.selectedClusterMemberTypeName })
          if (!selectedClusterMemberType.scalable) {
            console.log('ClusterMemberType is not scalable')
            return []
          }

          const downScalableNodes = []

          Object.entries(this.selectedClusterForScale.memberMapping)
              .filter(([k, v]) => {
                return v === selectedClusterMemberType.name
              }).forEach(([k, v]) => {
                downScalableNodes.push(this.selectedClusterForScale.nodes.find(n => n.ID === k))
              })

          if (selectedClusterMemberType.minNumber < downScalableNodes.length) {
            return downScalableNodes
          } else {
            return []
          }
        }
      },
      availableClusterMemberTypes () {
        if (this.selectedClusterForScale == null) {
          return []
        }

        const selectedClusterType = this.availableClusterTypes.find(type => {
          return type.name === this.selectedClusterForScale.clusterType
        })

        return selectedClusterType.clusterMemberTypes
      },
    },
    methods: {
      async scaleCluster () {
        const clusterUuid = this.selectedClusterForScale.id
        const resourceId = this.selectedBareMetalResource

        if (this.action === 'up') {
          ClustersRestApi.addMemberToCluster(clusterUuid, resourceId).then(response => {
            this.closeDialog()
          })
        }

        if (this.action === 'down') {
          ClustersRestApi.removeMemberFromCluster(clusterUuid, resourceId).then(response => {
            this.closeDialog()
          })
        }
      },
      closeDialog () {
        this.page = ''
        this.selectedClusterMemberTypeName = ''
        this.selectedBareMetalResource = ''
        this.$store.commit('SET_SELECTED_ClUSTER_FOR_SCALE', null)
      },
    },
  }

</script>
