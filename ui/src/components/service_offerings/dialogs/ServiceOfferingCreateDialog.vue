<template>
  <custom-dialog
    title="Create new service offering"
    :show="show"
    width="850"
    @canceled="$emit('canceled')"
  >
    <template
      #content
    >
      <v-container>
        <v-row>
          <v-col>
            <v-btn
              color="secondary"
              size="x-large"
              tile
              prepend-icon="mdi-account"
              @click="onAddServiceOfferingClicked('manual')"
            >
              Service Offering Wizard
            </v-btn>
          </v-col>
          <v-col>
            <v-btn
              color="secondary"
              size="x-large"
              tile
              prepend-icon="mdi-git"
              @click="onAddServiceOfferingClicked('git')"
            >
              Git-based Service Offering
            </v-btn>
          </v-col>
        </v-row>
      </v-container>
    </template>
    <template #actions>
      <div />
    </template>
  </custom-dialog>
</template>

<script>
import CustomDialog from "@/components/base/CustomDialog.vue";

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
      this.$router.push({ path: `/services/vendors/${this.serviceVendorId}/offerings`, query: {creationType: creationType} })
    },
  }
}
</script>

<style scoped>

</style>
