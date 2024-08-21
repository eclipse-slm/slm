<template>
  <v-container
    fluid
    tag="section"
  >
    <base-material-card color="secondary">
      <template #heading>
        <overview-heading text="Service Categories" />
      </template>

      <no-item-available-note
        v-if="serviceOfferingCategories == null || serviceOfferingCategories.length == 0"
        item="service vendors "
      />

      <v-data-table
        v-else
        :footer-props="{
          'items-per-page-options': [5, 10, 20, -1],
          pageText: $t('vuetify.dataIterator.pageText'),
          'items-per-page-text': $t('vuetify.dataIterator.itemsPerPageText'),
          'items-per-page-all-text': $t('vuetify.dataIterator.itemsPerPageAllText')
        }"
        :headers="ServiceCategoriesTableHeaders"
        item-key="id"
        :items="serviceOfferingCategories"
      >
        <template #item.actions="{ item }">
          <v-btn
            class="ma-1"
            size="small"
            color="info"
            @click="onEditServiceCategoryClicked(item)"
          >
            <v-icon>
              mdi-pencil
            </v-icon>
          </v-btn>

          <v-btn
            class="ma-1"
            size="small"
            color="error"
            @click="onDeleteServiceCategoryClicked(item)"
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
        @click="onAddServiceCategoryClicked"
      >
        Add Service Category
      </v-btn>
    </base-material-card>

    <service-category-create-or-edit-dialog
      :show="showCreateOrEditServiceCategoryDialog"
      :edit-mode="editServiceCategory"
      :service-category="selectedServiceCategory"
      @canceled="onCreateOrEditDialogCanceled"
      @confirmed="onCreateOrEditDialogConfirmed"
    />
  </v-container>
</template>

<script>

import ServiceCategoryCreateOrEditDialog from '@/components/service_offerings/ServiceCategoryCreateOrEditDialog'
import ServiceOfferingsRestApi from '@/api/service-management/serviceOfferingsRestApi'
import OverviewHeading from "@/components/base/OverviewHeading.vue";
import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";
import {app} from "@/main";
import {useServicesStore} from "@/stores/servicesStore";

export default {
    name: 'ServiceCategoriesTable',
    components: {OverviewHeading, ServiceCategoryCreateOrEditDialog, NoItemAvailableNote },
    setup(){
      const servicesStore = useServicesStore();
      return {servicesStore};
    },
    data () {
      return {
        selectedServiceCategory: null,
        editServiceCategory: false,
        showCreateOrEditServiceCategoryDialog: false,
      }
    },
    computed: {
      serviceOfferingCategories() {
        return this.servicesStore.serviceOfferingCategories
      },

      ServiceCategoriesTableHeaders () {
        return [
          { title: 'Name', value: 'name', sortable: true },
          { title: 'Id', value: 'id', sortable: true },
          { title: 'Actions', key: 'actions', sortable: false },
        ]
      },
    },
    created () {
      this.servicesStore.getServiceOfferingCategories();
    },
    methods: {
      onEditServiceCategoryClicked (serviceCategory) {
        this.selectedServiceCategory = serviceCategory
        this.editServiceCategory = true
        this.showCreateOrEditServiceCategoryDialog = true
      },
      onDeleteServiceCategoryClicked (serviceVendor) {
        this.selectedServiceCategory = null
        this.editServiceCategory = false
        ServiceOfferingsRestApi.deleteServiceCategory(serviceVendor.id).then(
          response => {
            app.config.globalProperties.$toast.info('Service category successfully deleted')

            this.servicesStore.getServiceOfferingCategories();
          })
          .catch(exception => {
            app.config.globalProperties.$toast.error('Failed to create service category')
            console.log('Service category deletion failed: ' + exception.response.data.message)
            console.log(exception)
          })
      },
      onAddServiceCategoryClicked () {
        this.selectedServiceCategory = {
          name: 'New Service Category',
        }
        this.editServiceCategory = false
        this.showCreateOrEditServiceCategoryDialog = true
      },
      onCreateOrEditDialogCanceled () {
        this.showCreateOrEditServiceCategoryDialog = false
        this.selectedServiceVendor = null
        this.editServiceCategory = false
      },
      onCreateOrEditDialogConfirmed () {
        this.showCreateOrEditServiceCategoryDialog = false
        this.selectedServiceCategory = null
        this.editServiceCategory = false

        this.servicesStore.getServiceOfferingCategories();
      },
    },
  }
</script>

<style scoped>
</style>
