<template>
  <v-container
    fluid
    tag="section"
  >
    <!-- Service Vendors !-->
    <base-material-card color="secondary">
      <template #heading>
        <overview-heading text="ServiceVendors" />
      </template>

      <no-item-available-note
        v-if="serviceVendors == null || serviceVendors.length == 0"
        item="service vendors "
      />

      <v-data-table
        v-if="serviceVendors!= null && serviceVendors.length > 0"
        :footer-props="{
          'items-per-page-options': [5, 10, 20, -1],
          pageText: $t('vuetify.dataIterator.pageText'),
          'items-per-page-text': $t('vuetify.dataIterator.itemsPerPageText'),
          'items-per-page-all-text': $t('vuetify.dataIterator.itemsPerPageAllText')
        }"
        :headers="ServiceVendorsTableHeaders"
        item-key="id"
        :items="serviceVendors"
      >
        <template
          #body="{ items }"
        >
          <tbody
            v-for="serviceVendor in items"
            :key="serviceVendor.id"
          >
            <tr @click="$emit('serviceVendorClicked', serviceVendor)">
              <td>{{ serviceVendor.name }}</td>
              <td>{{ serviceVendor.description }}</td>
              <td>{{ serviceVendor.id }}</td>
              <td>
                <v-btn
                  class="ma-1"
                  size="small"
                  color="info"
                  @click="onEditServiceVendorClicked(serviceVendor)"
                >
                  <v-icon>
                    mdi-pencil
                  </v-icon>
                </v-btn>

                <v-btn
                  class="ma-1"
                  size="small"
                  color="error"
                  @click="onDeleteServiceVendorClicked(serviceVendor)"
                >
                  <v-icon>
                    mdi-delete
                  </v-icon>
                </v-btn>
              </td>
            </tr>
          </tbody>
        </template>
      </v-data-table>
      <v-divider />
      <v-btn
        color="secondary"
        @click="onAddServiceVendorClicked"
      >
        Add Service Vendor
      </v-btn>
    </base-material-card>

    <service-vendor-create-or-edit-dialog
      :show="showCreateOrEditServiceVendorDialog"
      :edit-mode="editServiceVendor"
      :service-vendor="selectedServiceVendor"
      @canceled="onCreateOrEditDialogCanceled"
      @confirmed="onCreateOrEditDialogConfirmed"
    />
  </v-container>
</template>

<script>
  import { mapGetters } from 'vuex'
  import ServiceVendorCreateOrEditDialog from '@/components/service_vendors/ServiceVendorCreateOrEditDialog'
  import ServiceVendorsRestApi from '@/api/service-management/serviceVendorsRestApi'
  import Vue from 'vue'
  import OverviewHeading from "@/components/base/OverviewHeading.vue";
  import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
  import {app} from "@/main";

  export default {
    name: 'ServiceVendorTable',
    components: {NoItemAvailableNote, OverviewHeading, ServiceVendorCreateOrEditDialog },
    data () {
      return {
        selectedServiceVendor: null,
        editServiceVendor: false,
        showCreateOrEditServiceVendorDialog: false,
      }
    },
    computed: {
      ...mapGetters([
        'serviceVendors',
      ]),
      ServiceVendorsTableHeaders () {
        return [
          { text: 'Name', value: 'serviceVendorName', sortable: true },
          { text: 'Description', value: 'serviceVendorDescription', sortable: false },
          { text: 'Id', value: 'serviceVendorId', sortable: true },
          { text: 'Actions', value: 'serviceVendorActions', sortable: false },
        ]
      },
    },
    created () {
      this.$store.dispatch('getServiceVendors')
    },
    methods: {
      onEditServiceVendorClicked (serviceVendor) {
        this.selectedServiceVendor = serviceVendor
        this.editServiceVendor = true
        this.showCreateOrEditServiceVendorDialog = true
      },
      onDeleteServiceVendorClicked (serviceVendor) {
        this.selectedServiceVendor = null
        this.editServiceVendor = false
        ServiceVendorsRestApi.deleteServiceVendorById(serviceVendor.id).then(
          response => {
            app.config.globalProperties.$toast.info('Service vendor successfully deleted')
            this.$store.dispatch('getServiceVendors')
          })
          .catch(exception => {
            app.config.globalProperties.$toast.error('Failed to create service offering')
            console.log('Service vendor deletion failed: ' + exception.response.data.message)
            console.log(exception)
          })
      },
      onAddServiceVendorClicked () {
        this.selectedServiceVendor = {
          name: 'New Service Vendor',
          description: 'My new service vendor',
        }
        this.editServiceVendor = false
        this.showCreateOrEditServiceVendorDialog = true
      },
      onCreateOrEditDialogCanceled () {
        this.showCreateOrEditServiceVendorDialog = false
        this.selectedServiceVendor = null
        this.editServiceVendor = false
      },
      onCreateOrEditDialogConfirmed () {
        this.showCreateOrEditServiceVendorDialog = false
        this.selectedServiceVendor = null
        this.editServiceVendor = false
        this.$store.dispatch('getServiceVendors')
      },
    },
  }
</script>

<style scoped>
</style>
