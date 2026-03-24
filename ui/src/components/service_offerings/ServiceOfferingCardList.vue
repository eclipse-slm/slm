<template>
  <v-card
    class="mx-1 my-1"
    :elevation="hovered"
    variant="outlined"
    :disabled="passive"
    @mouseenter="hovered = 24"
    @mouseleave="hovered = 0"
    @click="serviceClicked(service)"
  >
    <v-container>
      <v-list-item>
        <v-list-item-avatar><v-img :src="'data:image/jpeg;base64,' + serviceVendorById(service.serviceVendorId).logo" /></v-list-item-avatar>
        <v-list-item>
          <v-list-item-title class="text-h5">
            {{ service.title }}
          </v-list-item-title>
          <v-list-item-subtitle>{{ serviceVendorById(service.serviceVendorId).name }}</v-list-item-subtitle>
        </v-list-item>
      </v-list-item>

      <v-row class="mx-2 my-2">
        <v-col id="serviceCardContent_category">
          {{ service.category }}
        </v-col>
        <v-col id="serviceCardContent_version">
          {{ service.version }}
        </v-col>
      </v-row>

      <v-row class="mx-2 my-2">
        <v-col id="serviceCardContent_deploymentType">
          {{ service.deploymentCapability }}
        </v-col>
        <v-col id="serviceCardContent_cost">
          {{ service.cost }}
        </v-col>
      </v-row>

      <v-row>
        <v-img
          :src="'data:image/jpeg;base64,' + service.coverImage"
          height="300"
          width="300"
        />
      </v-row>

      <v-row>
        <v-card-text>
          {{ service.shortDescription }}
        </v-card-text>
      </v-row>
    </v-container>
  </v-card>
</template>

<script>

import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";
import {storeToRefs} from "pinia";

export default {
    name: 'ServiceOfferingCardList',
    props: ['service', 'imgWidth', 'passive'],
    setup(){
      const serviceOfferingsStore = useServiceOfferingsStore();
      const {serviceVendorById} = storeToRefs(serviceOfferingsStore)
      return {serviceOfferingsStore, serviceVendorById};
    },
    data: function () {
      return {
        hovered: 0,
      }
    },
    computed: {
    },
    methods: {
      serviceClicked (selectedService) {
        this.$router.push({ path: `/services/${selectedService.id}` })
      },
    },

  }
</script>

<style rel="stylesheet"
       type="text/css"
       src="@/design/serviceOfferingCard.scss"
       lang="scss" scoped
>
</style>

<style lang="scss" scoped>
#serviceCardContent_cost {
  text-align: end;
  font-weight: bold;
}

#serviceCardContent_version {
  text-align: end;
  font-weight: bold;
}

#serviceCardContent_deploymentType{
  text-align: left;
  font-weight: bold;
}

#serviceCardContent_category{
  text-align: left;
  font-weight: bold;
}
</style>
