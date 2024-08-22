<template>
  <div>
    <base-material-card>
      <template #heading>
        <overview-heading text="Provider" />
      </template>

      <v-card flat>
        <v-tabs
          v-model="tab"
          align-tabs="title"
          bg-color="bg-grey-lighten-5"
        >
          <v-tab
            v-for="item in provider"
            :key="item.id"
          >
            {{ item.title }}
          </v-tab>
        </v-tabs>

        <v-window-item
          v-for="item in provider"
          :key="item.id"
        >
          <v-card flat>
            <v-card-text>
              <virtual-resource-provider-table v-if="item.id==='VirtualResourceProvider'" />
              <service-hoster-table v-if="item.id==='ServiceHosted'" />
            </v-card-text>
          </v-card>
        </v-window-item>
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
