<template>
  <v-card
    class="mx-1 my-1"
    variant="outlined"
    style="border: thin solid rgba(0,0,0,.12) !important;"
    :elevation="hovered"
    height="100%"
    @mouseenter="passive ? hovered = 0 : hovered = 24"
    @mouseleave="hovered = 0"
    @click="$emit('click', serviceOffering)"
  >
    <v-container
      fluid
      grid-list-md
    >
      <v-row no-gutters>
        <v-col>
          <v-list-item :prepend-avatar="getImageUrl(serviceVendorById(serviceOffering.serviceVendorId).logo)">
            <v-list-item-title class="text-h5">
              {{ serviceOffering.name }}
            </v-list-item-title>
            <v-list-item-subtitle>{{ serviceVendorById(serviceOffering.serviceVendorId).name }}</v-list-item-subtitle>
          </v-list-item>
        </v-col>
      </v-row>

      <v-row class="mx-2 my-2">
        <v-col id="serviceCardContent_category">
          <div>
            <text-with-label
              label="Category"
              :text="serviceOfferingCategoryNameById(serviceOffering.serviceCategoryId)"
            />
          </div>
        </v-col>
        <v-col id="serviceCardContent_version">
          <text-with-label
            v-if="showOnlyLatestVersion || serviceOffering.versions.length == 1"
            label="Latest Version"
            :text="latestVersion.version"
          />
          <v-select
            v-else
            v-model="selectedServiceOfferingVersion"
            label="Versions"
            :items="serviceOffering.versions"
            item-title="version"
            item-value="id"
            @update:modelValue="onServiceOfferingVersionSelected"
          />
        </v-col>
      </v-row>

      <v-row>
        <v-img
          ref="coverImage"
          :src="createOrEditMode ? getImageUrl(serviceOffering.coverImage) : coverImage"
          :aspect-ratio="4/3"
        >
          <template
            v-if="!createOrEditMode"
            #placeholder
          >
            <v-row
              class="fill-height ma-0"
              align="center"
              justify="center"
            >
              <progress-circular v-if="!getImageUrl(serviceOffering.coverImage).includes('none')" />
            </v-row>
          </template>
        </v-img>
      </v-row>

      <v-row>
        <div style="margin: 16px">
          {{ limitShortDescriptionLength(serviceOffering.shortDescription) }}
        </div>
      </v-row>
    </v-container>
  </v-card>
</template>

<script>

import TextWithLabel from '@/components/base/TextWithLabel'
import getImageUrl from '@/utils/imageUtil'
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";
import {storeToRefs} from "pinia";

export default {
    name: 'ServiceOfferingCardGrid',
    components: {ProgressCircular, TextWithLabel },
    props: ['serviceOffering', 'imgWidth', 'passive', 'createOrEditMode', "showOnlyLatestVersion"],
    setup(){
      const serviceOfferingsStore = useServiceOfferingsStore();
      const {serviceVendorById, serviceOfferingCategoryNameById, serviceOfferingDeploymentTypePrettyName} = storeToRefs(serviceOfferingsStore);
      return {serviceOfferingsStore, serviceVendorById, serviceOfferingCategoryNameById, serviceOfferingDeploymentTypePrettyName};
    },
    data: function () {
      return {
        hovered: 0,
        shortDescriptionLengthLimit: 160,
        coverImage: null,
        selectedServiceOfferingVersion: null,
      }
    },
    computed: {

      latestVersion() {
        let latestVersionFound = {}
        if (this.serviceOffering.versions?.length > 0) {
          latestVersionFound = this.serviceOffering.versions[0]
          this.serviceOffering.versions.forEach(version => {
            let createdDateLatest = new Date(latestVersionFound.created)
            let createdDateCurrent = new Date(version.created)
            if (createdDateCurrent > createdDateLatest) {
              latestVersionFound = version
            }
          })
        } else {
          latestVersionFound = { id: "00000000-0000-0000-0000-000000000000", "version": "No versions" }
        }

        return latestVersionFound
      }
    },
    mounted () {
      if (this.serviceOffering.id !== undefined) {
        this.serviceOfferingsStore.getServiceOfferingImages(this.serviceOffering.id).then(coverImage => {
          this.coverImage = coverImage
        });
      }

      if (this.serviceOffering.versions?.length > 0) {
        this.serviceOffering.versions.sort(function (a, b) {
          return new Date(b.created) - new Date(a.created);
        })
      }
      this.selectedServiceOfferingVersion = this.latestVersion
      if (this.serviceOffering.versions?.length > 0) {
        this.$emit('service-offering-version-selected', this.selectedServiceOfferingVersion.id)
      }

    },
    methods: {
      getImageUrl (imageData) {
        let imageUrl = getImageUrl(imageData)
        return imageUrl
      },
      limitShortDescriptionLength (shortDescription) {
        if (typeof shortDescription === 'string' && shortDescription.length > this.shortDescriptionLengthLimit) {
          return shortDescription.slice(0, this.shortDescriptionLengthLimit) + '...'
        } else {
          return shortDescription
        }
      },
      onServiceOfferingVersionSelected (serviceOfferingVersionId) {
        this.$emit('service-offering-version-selected', serviceOfferingVersionId)
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
