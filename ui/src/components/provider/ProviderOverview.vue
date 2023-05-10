<template>
  <div>
    <base-material-card
      class="px-5 py-3"
    >
      <template #heading>
        <overview-heading text="Provider" />
      </template>

      <v-card flat>
        <v-tabs
          v-model="tab"
          align-with-title
          background-color="grey lighten-5"
        >
          <v-tab
            v-for="item in provider"
            :key="item.id"
          >
            {{ item.title }}
          </v-tab>
        </v-tabs>

        <v-tabs-items v-model="tab">
          <v-tab-item
            v-for="item in provider"
            :key="item.id"
          >
            <v-card flat>
              <v-card-text>
                <virtual-resource-provider-table v-if="item.id=='VirtualResourceProvider'" />
                <service-hoster-table v-if="item.id=='ServiceHoster'" />
              </v-card-text>
            </v-card>
          </v-tab-item>
        </v-tabs-items>
      </v-card>
    </base-material-card>
  </div>
</template>

<script>
import OverviewHeading from "@/components/base/OverviewHeading.vue";
import VirtualResourceProviderTable from "@/components/provider/VirtualResourceProviderTable.vue";
import ServiceHosterTable from "@/components/provider/ServiceHosterTable.vue";
export default {
  name: 'ProviderOverview',
  components: {ServiceHosterTable, VirtualResourceProviderTable, OverviewHeading},
  data() {
    return {
      tab: null,
      provider: [
        {
          id: 'VirtualResourceProviders',
          title: 'Virtual Resource Providers',
          text: 'Here are the virtual resource provider'
        },
        {
          id: 'ServiceHosters',
          title: 'Service Hosters',
          text: 'Here are the service hoster'
        }
      ]
    }
  }
}
</script>
