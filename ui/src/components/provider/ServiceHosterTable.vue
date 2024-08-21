<template>
  <div>
    <no-item-available-note
      v-if="!serviceHosters.length"
      item="Service Hoster"
    />
    <v-data-table
      v-else
      id="service-hoster-table"
      :headers="tableHeaders"
      :items="serviceHosters"
    />
  </div>
</template>

<script>

import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import {useServicesStore} from "@/stores/servicesStore";

export default {
  name: 'ServiceHosterTable',
  components: {NoItemAvailableNote},
  setup(){
    const servicesStore = useServicesStore();
    return {servicesStore}
  },
  data() {
    return {
      tableHeaders: [
        { title: 'ID',             value: 'capabilityService.ID' },
        { title: 'Provider Name',  value: 'capabilityService.service'},
        { title: 'Cluster',        value: 'capabilityService.capability.cluster' },
        { title: 'Machine ID',     value: 'capabilityService.consulNodeId' },
        {                         value: 'actions', sortable: false }
      ],
      showCreateDialog: false,
      selectedVirtualResourceProvider: null,
    }
  },
  computed: {
    serviceHosters () {
      return this.servicesStore.serviceHosters
    }
  },
}
</script>
