<template>
  <v-dialog
    v-model="active"
    width="600"
    @click:outside="$emit('canceled')"
  >
    <template #default="{}">
      <v-card v-if="active">
        <v-toolbar
          color="primary"
          theme="dark"
        >
          Delete Cluster
        </v-toolbar>
        <v-card-text
          v-if="serviceInstancesForCluster.length === 0"
          class="mt-2"
        >
          Do you want to delete '{{ cluster.clusterType }}' cluster '{{ cluster.name }}'?
        </v-card-text>
        <v-card-text v-else>
          <v-alert
            prominent
            type="error"
          >
            <div>
              There are '{{ serviceInstancesForCluster.length }}' service instances running on this cluster:<br>
              <v-list-item
                v-for="item in serviceInstancesForCluster"
                :key="item.id"
              >
                <v-list-item>
                  <v-icon>mdi-apps</v-icon>
                </v-list-item>
                <v-list-item>
                  <v-list-item-title> {{ item.id }}</v-list-item-title>
                  <v-list-item-subtitle>
                    ({{ serviceOfferingById(item.serviceOfferingId).name }} - Version: {{ serviceOfferingById(item.serviceOfferingId).versions.find(version => version.id === item.metaData.service_offering_version_id)?.version }})
                  </v-list-item-subtitle>
                </v-list-item>
              </v-list-item>
              <strong>Cannot delete!</strong>
            </div>
          </v-alert>
        </v-card-text>
        <v-card-actions class="justify-center">
          <v-spacer />

          <v-tooltip location="bottom">
            <template #activator="{ props }">
              <v-btn
                variant="elevated"
                v-bind="props"
                color="error"
                :disabled="serviceInstancesForCluster.length > 0"
                @click="deleteCluster"
              >
                Yes
              </v-btn>
            </template>
            <span>There are no known services running on the cluster</span>
          </v-tooltip>

          <v-btn
            variant="elevated"
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
import {toRef} from "vue";
import {useServiceInstancesStore} from "@/stores/serviceInstancesStore";
import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";
import {storeToRefs} from "pinia";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";

export default {
    name: 'ClusterDeleteDialog',
    props:{
      showDialog: {
        type: Boolean,
        default: false
      },
      cluster: {
        type: Object,
        default: null
      }
    },
    setup(props){
      const active = toRef(props, 'showDialog')
      const serviceInstancesStore = useServiceInstancesStore();
      const serviceOfferingsStore = useServiceOfferingsStore();
      const {serviceOfferingById} = storeToRefs(serviceOfferingsStore)
      return{
        active, serviceInstancesStore, serviceOfferingById
      }
    },
    computed: {
      services() {
        return this.serviceInstancesStore.services
      },

      serviceInstancesForCluster(){
        console.log(this.services)
        return this.services.filter(svc => svc.resourceId === this.cluster.id)
      },
    },
    methods: {
      deleteCluster () {
        ResourceManagementClient.clusterApi
            .deleteClusterResource(this.cluster.id)
            .then().catch(logRequestError);
        this.$emit('confirmed')
      },
    }
  }
</script>
