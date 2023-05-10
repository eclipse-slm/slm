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
        <template
          #body="{ items }"
        >
          <tbody
            v-for="serviceCategory in items"
            :key="serviceCategory.id"
          >
            <tr>
              <td>{{ serviceCategory.name }}</td>
              <td>{{ serviceCategory.id }}</td>
              <td>
                <v-btn
                  class="ma-1"
                  small
                  color="info"
                  @click="onEditServiceCategoryClicked(serviceCategory)"
                >
                  <v-icon>
                    mdi-pencil
                  </v-icon>
                </v-btn>

                <v-btn
                  class="ma-1"
                  small
                  color="error"
                  @click="onDeleteServiceCategoryClicked(serviceCategory)"
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
  import { mapGetters } from 'vuex'
  import ServiceCategoryCreateOrEditDialog from '@/components/service_offerings/ServiceCategoryCreateOrEditDialog'
  import ServiceOfferingsRestApi from '@/api/service-management/serviceOfferingsRestApi'
  import Vue from 'vue'
  import OverviewHeading from "@/components/base/OverviewHeading.vue";
  import NoItemAvailableNote from "@/components/base/NoItemAvailableNote.vue";

  export default {
    name: 'ServiceCategoriesTable',
    components: {OverviewHeading, ServiceCategoryCreateOrEditDialog, NoItemAvailableNote },
    data () {
      return {
        selectedServiceCategory: null,
        editServiceCategory: false,
        showCreateOrEditServiceCategoryDialog: false,
      }
    },
    computed: {
      ...mapGetters([
        'serviceOfferingCategories',
      ]),
      ServiceCategoriesTableHeaders () {
        return [
          { text: 'Name', value: 'serviceCategoryName', sortable: true },
          { text: 'Id', value: 'serviceCategoryId', sortable: true },
          { text: 'Actions', value: 'serviceCategoryActions', sortable: false },
        ]
      },
    },
    created () {
      this.$store.dispatch('getServiceOfferingCategories')
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
            Vue.$toast.info('Service category successfully deleted')
            this.$store.dispatch('getServiceOfferingCategories')
          })
          .catch(exception => {
            Vue.$toast.error('Failed to create service category')
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
        this.$store.dispatch('getServiceOfferingCategories')
      },
    },
  }
</script>

<style scoped>
</style>
