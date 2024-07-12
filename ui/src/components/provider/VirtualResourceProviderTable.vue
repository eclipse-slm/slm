<template>
  <div>
    <no-item-available-note
      v-if="!virtualResourceProviders.length"
      item="Virtual Resource Provider"
    />
    <v-data-table
      v-else
      id="virtual-resource-provider-table"
      :headers="tableHeaders"
      :items="virtualResourceProviders"
      item-key=""
    >
      <template #item.actions="{item}">
        <div class="text-right">
          <v-btn
            color="info"
            disabled
            @click="showDialog(item)"
          >
            <v-icon icon="mdi-plus-thick" />
          </v-btn>
        </div>
      </template>
    </v-data-table>
    <vm-create-dialog
      v-if="showCreateDialog"
      :virtual-resource-provider="selectedVirtualResourceProvider"
      :show="showCreateDialog"
      @canceled="showCreateDialog = false"
    />
  </div>
</template>

<script>
import {mapGetters} from "vuex";
import VmCreateDialog from "@/components/provider/dialogs/VmCreateDialog.vue";
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import {useResourcesStore} from "@/stores/resourcesStore";
import {useProviderStore} from "@/stores/providerStore";

export default {
  name: 'VirtualResourceProviderTable',
  components: {VmCreateDialog, NoItemAvailableNote},
  setup(){
    const providerStore = useProviderStore();
    return {providerStore}
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
  computed: {
    virtualResourceProviders() {
      return this.providerStore.virtualResourceProviders
    },
  },
  methods: {
    showDialog(CapabilityServiceID) {
      this.showCreateDialog = true
      this.selectedVirtualResourceProvider = CapabilityServiceID
    }
  }
}
</script>
