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
import {mapGetters} from "vuex";
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";

export default {
  name: 'ServiceHosterTable',
  components: {NoItemAvailableNote},
  computed: {
    ...mapGetters([
        'serviceHosters'
    ])
  },
  data() {
    return {
      tableHeaders: [
        { text: 'ID',             value: 'capabilityService.ID' },
        { text: 'Provider Name',  value: 'capabilityService.service'},
        { text: 'Cluster',        value: 'capabilityService.capability.cluster' },
        { text: 'Machine ID',     value: 'capabilityService.consulNodeId' },
        {                         value: 'actions', sortable: false }
      ],
      showCreateDialog: false,
      selectedVirtualResourceProvider: null,
    }
  },
}
</script>
