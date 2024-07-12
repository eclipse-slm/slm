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
        @click:row="onRowClick"
      >
        <template #item.actions="{ item }">
          <v-btn
            class="ma-1"
            size="small"
            color="info"
            @click="onEditServiceVendorClicked(item)"
          >
            <v-icon>
              mdi-pencil
            </v-icon>
          </v-btn>

          <v-btn
            class="ma-1"
            size="small"
            color="error"
            @click="onDeleteServiceVendorClicked(item)"
          >
            <v-icon>
              mdi-delete
            </v-icon>
          </v-btn>
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
  import {useServicesStore} from "@/stores/servicesStore";

  export default {
    name: 'ServiceVendorTable',
    components: {NoItemAvailableNote, OverviewHeading, ServiceVendorCreateOrEditDialog },
    setup(){
      const servicesStore = useServicesStore();
      return {servicesStore};
    },
    data () {
      return {
        selectedServiceVendor: null,
        editServiceVendor: false,
        showCreateOrEditServiceVendorDialog: false,
      }
    },
    computed: {
      serviceVendors () {
        return this.servicesStore.serviceVendors
      },
      ServiceVendorsTableHeaders () {
        return [
          { title: 'Name', key: 'name', sortable: true },
          { title: 'Description', key: 'description',  sortable: false },
          { title: 'Id', key: 'id', sortable: true },
          { title: 'Actions', key: 'actions', sortable: false },
        ]
      },
    },
    created () {
      this.servicesStore.getServiceVendors();
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
            this.servicesStore.getServiceVendors();
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
        this.servicesStore.getServiceVendors();
      },
      onRowClick(click, row){
        this.$emit('serviceVendorClicked', row.item)
      }
    },
  }
</script>

<style scoped>
</style>
