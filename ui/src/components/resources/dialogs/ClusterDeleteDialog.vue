<template>
  <v-dialog
    v-model="showDialog"
    width="600"
    @click:outside="$emit('canceled')"
  >
    <template>
      <v-card v-if="showDialog">
        <v-toolbar
          color="primary"
          dark
        >
          Delete Cluster
        </v-toolbar>
        <v-card-text v-if="serviceInstancesForCluster.length === 0">
          Do your really want to delete '{{ cluster.clusterType }}' cluster '{{ cluster.name }}'?
        </v-card-text>
        <v-card-text v-else>
          <v-alert prominent type="error">
            <div>
              There are '{{ serviceInstancesForCluster.length }}' service instances running on this cluster:<br>
              <v-list-item v-for="item in serviceInstancesForCluster" v-bind:key="item.id">
                <v-list-item-icon>
                  <v-icon>mdi-apps</v-icon>
                </v-list-item-icon>
                <v-list-item-content>
                  <v-list-item-title> {{ item.id }}</v-list-item-title>
                  <v-list-item-subtitle>
                    ({{ serviceOfferingById(item.serviceOfferingId).name }} - Version: {{ serviceOfferingById(item.serviceOfferingId).versions.find(version => version.id === item.metaData.service_offering_version_id)?.version }})
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
              <strong>Cannot delete!</strong>
            </div>
          </v-alert>
        </v-card-text>
        <v-card-actions class="justify-center">
          <v-spacer />

          <v-tooltip bottom>
            <template v-slot:activator="{ on, attrs }">
              <v-btn
                  v-bind="attrs"
                  v-on="on"
                  color="error"
                  @click="deleteCluster"
                  :disabled="serviceInstancesForCluster.length > 0"
              >
                Yes
              </v-btn>
            </template>
            <span>There are no known services running on the cluster</span>
          </v-tooltip>

          <v-btn
            color="info"
            @click="$emit('canceled')"
          >
            No
          </v-btn>
        </v-card-actions>
      </v-card>
    </template>
  </v-dialog>
</template>

<script>
  import ClustersRestApi from '@/api/resource-management/clustersRestApi.js'
  import { mapGetters } from "vuex";

  export default {
    name: 'ClusterDeleteDialog',
    props: ['showDialog', 'cluster'],
    methods: {
      deleteCluster () {
        ClustersRestApi.deleteClusterResource(this.cluster.id)
        this.$emit('confirmed')
      },
    },
    computed: {
      ...mapGetters([
        'services',
        'serviceOfferingById'
      ]),
      serviceInstancesForCluster(){
        console.log(this.services)
        return this.services.filter(svc => svc.resourceId === this.cluster.id)
      },
    },
  }
</script>
