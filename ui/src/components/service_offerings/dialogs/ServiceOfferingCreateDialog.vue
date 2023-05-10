<template>
  <custom-dialog
    title="Create new service offering"
    :show="show"
    width="800"
    @canceled="$emit('canceled')"
  >
    <template
      #content
    >
      <v-row>
        <v-col>
          <v-btn
            color="secondary"
            x-large
            tile
            @click="onAddServiceOfferingClicked('manual')"
          >
            <v-icon
              class="mr-5 ml-2"
              large
              left
            >
              mdi-account
            </v-icon>
            Service Offering Wizard
          </v-btn>
        </v-col>
        <v-col>
          <v-btn
            color="secondary"
            x-large
            tile
            @click="onAddServiceOfferingClicked('git')"
          >
            <v-icon
              class="mr-5 ml-2"
              large
              left
            >
              mdi-git
            </v-icon>
            Git-based Service Offering
          </v-btn>
        </v-col>
      </v-row>
    </template>
    <template #actions>
      <div />
    </template>
  </custom-dialog>
</template>

<script>
import CustomDialog from "@/components/base/CustomDialog";

export default {
  name: "ServiceOfferingCreateDialog",
  components: {
    CustomDialog,
  },
  props: {
    show: {
      default: false,
      type: Boolean
    },
    serviceVendorId: {
      required: true,
      type: String
    }
  },
  methods: {
    onAddServiceOfferingClicked (creationType) {
      this.$emit('selected')
      this.$router.push({ path: `/services/vendors/${this.serviceVendorId}/offerings?creationType=${creationType}` })
    },
  }
}
</script>

<style scoped>

</style>
