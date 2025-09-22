<template>
  <div>
    <div
      v-if="apiState === ApiState.INIT || apiState === ApiState.LOADING || apiState === ApiState.UPDATING"
      class="text-center"
    >
      <ProgressCircular />
    </div>
    <div v-if="apiState === ApiState.ERROR">
      Error
    </div>

    <v-container
      v-if="apiState === ApiState.LOADED"
      id="user-profile"
      fluid
      tag="section"
    >
      <v-row
        justify="center"
      >
        <v-col
          cols="12"
          md="8"
        >
          <v-select
            v-model="selectedServiceVendor"
            :items="serviceVendorsOfDeveloper"
            item-title="name"
            return-object
            label="Select Service Vendor"
            autofocus
            @update:modelValue="onServiceVendorSelected"
          />
        </v-col>
      </v-row>

      <v-row
        v-if="selectedServiceVendor != null"
        justify="center"
      >
        <v-col
          cols="12"
          md="7"
        >
          <!-- Service Offerings !-->
          <base-material-card
            color="secondary"
          >
            <template #heading>
              <overview-heading text="Service Offerings" />
            </template>

            <div
              v-if="serviceOfferingsOfVendor == null && serviceOfferingsOfVendorLoaded"
            >
              <v-alert
                variant="outlined"
                type="info"
              >
                No service offerings available
              </v-alert>
            </div>
            <service-offering-table
              v-if="serviceOfferingsOfVendor != null && serviceOfferingsOfVendorLoaded"
              :service-offerings="serviceOfferingsOfVendor"
              @interface="getServiceOfferingTableInterface"
              @service-offering-delete="onDeleteServiceOffering"
              @service-offering-version-delete="onDeleteServiceOfferingVersion"
            />
            <v-divider />
            <v-btn
              color="secondary"
              @click="serviceOfferingCreateDialog = true"
            >
              <v-icon
                density="compact"
                size="small"
                class="mr-2"
              >
                mdi-plus
              </v-icon>
              Add Service Offering

              <service-offering-create-dialog
                :show="serviceOfferingCreateDialog"
                :service-vendor-id="selectedServiceVendor.id"
                @canceled="serviceOfferingCreateDialog = false"
                @selected="serviceOfferingCreateDialog = false"
              />
            </v-btn>
            <!-- Service Offering Delete Dialog !-->
            <confirm-dialog
              v-if="serviceOfferingDeleteDialog"
              :show="serviceOfferingDeleteDialog"
              title="Delete service offering?"
              :text="`Do you want to delete service offering '${serviceOfferingToDelete.name}'?`"
              @canceled="serviceOfferingDeleteDialog = false"
              @confirmed="onDeleteServiceOfferingConfirmed"
            />

            <!-- Service Offering Version Delete Dialog !-->
            <confirm-dialog
              v-if="serviceOfferingVersionDeleteDialog"
              :show="serviceOfferingVersionDeleteDialog"
              title="Delete service offering version?"
              :text="`Do you want to delete service offering version '${serviceOfferingVersionToDelete.version}'?`"
              @canceled="serviceOfferingVersionDeleteDialog = false"
              @confirmed="onDeleteServiceOfferingVersionConfirmed"
            />
          </base-material-card>

          <!-- Repositories !-->
          <base-material-card color="secondary">
            <template #heading>
              <overview-heading text="Repositories" />
            </template>

            <v-data-table
              :headers="RepositoriesTableHeaders"
              item-key="id"
              :items="repositoriesOfVendor"
            >
              <template #item.password="{ }">
                <v-text-field
                  label="Encrypted"
                  size="small"
                  disabled
                />
              </template>
              <template #item.actions="{ item : serviceRepository }">
                <v-btn
                  class="ma-2"
                  color="error"
                  @click="onDeleteRepositoryClicked(serviceRepository)"
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
              @click="serviceRepositoryCreateDialog = true"
            >
              <v-icon
                density="compact"
                size="small"
                class="mr-2"
              >
                mdi-plus
              </v-icon>
              Add Repository
            </v-btn>
          </base-material-card>
        </v-col>

        <v-col
          cols="12"
          md="5"
        >
          <v-row>
            <v-col cols="12">
              <base-material-card
                class="v-card-profile"
                :avatar="getImageUrl(selectedServiceVendor.logo)"
              >
                <v-card-text class="text-center">
                  <div class="text-h4 font-weight-light mb-3 text-black">
                    {{ selectedServiceVendor.name }}
                  </div>

                  <p>
                    ID: {{ selectedServiceVendor.id }}
                  </p>

                  <p>
                    {{ selectedServiceVendor.description }}
                  </p>

                  <v-btn
                    color="secondary"
                    class="mr-0"
                    @click="showEditServiceVendorDialog = true"
                  >
                    <v-icon
                      density="compact"
                      size="small"
                      class="mr-2"
                    >
                      mdi-pencil
                    </v-icon>
                    Edit Profile
                  </v-btn>
                </v-card-text>
              </base-material-card>
            </v-col>
          </v-row>
          <v-row>
            <v-col cols="12">
              <!-- Developers !-->
              <service-vendors-developers-table
                v-if="selectedServiceVendor != null"
                :service-vendor="selectedServiceVendor"
              />
            </v-col>
          </v-row>
        </v-col>
      </v-row>
      <!-- Service Repository Create Dialog !-->
      <service-repository-create-dialog
        :show="serviceRepositoryCreateDialog"
        @canceled="serviceRepositoryCreateDialog = false"
        @confirmed="onServiceRepositoryCreateDialogConfirmed"
      />
    </v-container>

    <service-vendor-create-or-edit-dialog
      :show="showEditServiceVendorDialog"
      :edit-mode="true"
      :service-vendor="selectedServiceVendor"
      @canceled="showEditServiceVendorDialog = false"
      @confirmed="onEditServiceVendorDialogConfirmed"
    />
  </div>
</template>

<script>

import ApiState from '@/api/apiState'
import ServiceRepositoryCreateDialog from '@/components/service_vendors/ServiceRepositoryCreateDialog'
import ServiceVendorsDevelopersTable from '@/components/service_vendors/ServiceVendorDevelopersTable'
import ServiceVendorCreateOrEditDialog from '@/components/service_vendors/ServiceVendorCreateOrEditDialog'
import logRequestError from '@/api/restApiHelper'
import getImageUrl from '@/utils/imageUtil'
import ServiceOfferingTable from "@/components/service_offerings/ServiceOfferingTable.vue";
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";
import ServiceOfferingCreateDialog from "@/components/service_offerings/dialogs/ServiceOfferingCreateDialog.vue";
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import OverviewHeading from "@/components/base/OverviewHeading.vue";
import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";
import {useStore} from "@/stores/store";
import {useUserStore} from "@/stores/userStore";
import {storeToRefs} from "pinia";
import ServiceManagementClient from "@/api/service-management/service-management-client";

export default {
    components: {
      OverviewHeading,
      ProgressCircular,
      ServiceOfferingCreateDialog,
      ConfirmDialog,
      ServiceOfferingTable,
      ServiceRepositoryCreateDialog,
      ServiceVendorsDevelopersTable,
      ServiceVendorCreateOrEditDialog,
    },
    props: {
      serviceVendorId: {
        type: String,
        default: null
      }
    },
    setup(){
      const store = useStore();
      const userStore = useUserStore();
      const serviceOfferingsStore = useServiceOfferingsStore();
      const {apiState, serviceVendorById, serviceOfferingCategoryNameById} = storeToRefs(serviceOfferingsStore)
      return {apiState, store, userStore, serviceOfferingsStore, serviceVendorById, serviceOfferingCategoryNameById};
    },
    data () {
      return {
        serviceOfferings: [],

        serviceVendorsOfDeveloper: [],
        selectedServiceVendor: null,

        serviceOfferingsOfVendor: [],
        serviceOfferingsOfVendorLoaded: false,
        serviceOfferingCreateDialog: false,
        serviceOfferingDeleteDialog: false,
        serviceOfferingVersionDeleteDialog: false,
        serviceOfferingToDelete: null,
        serviceOfferingVersionToDelete: null,

        availableDevelopers: null,

        addedDevelopers: [],
        developersOfServiceVendor: [],

        repositoriesOfVendor: [],
        serviceRepositoryCreateDialog: false,

        showEditServiceVendorDialog: false,
      }
    },
    computed: {
      ApiState() {
        return ApiState
      },
      userId () {
        return this.userStore.userId
      },
      RepositoriesTableHeaders () {
        return [
          { title: 'Id', value: 'id', sortable: false },
          { title: 'Address', value: 'address', sortable: true },
          { title: 'Username', value: 'username', sortable: false },
          { title: 'Password', value: 'password', sortable: false },
          { title: 'Type', value: 'type', sortable: true },
          { title: 'Actions', value: 'actions', sortable: false },
        ]
      },
    },
    created () {
      this.serviceOfferingsStore.getServiceVendors().then(() => {
        ServiceManagementClient.usersApi.getServiceVendorsOfUser(this.userId).then(response => {
          this.serviceVendorsOfDeveloper = []
          response.data.forEach(serviceVendorId => {
            if (this.serviceVendorId != undefined) {
              if (this.serviceVendorId == serviceVendorId) {
                this.selectedServiceVendor = this.serviceVendorById(serviceVendorId)
                this.onServiceVendorSelected()
              }
            }
            this.serviceVendorsOfDeveloper.push(this.serviceVendorById(serviceVendorId))
          })
        }).catch(logRequestError);
      }
      );
    },
    methods: {
      getServiceOfferingTableInterface (serviceOfferingTableInterface) {
        this.$options.serviceOfferingTableInterface = serviceOfferingTableInterface;
      },
      getImageUrl (imageData) { return getImageUrl(imageData) },
      onDeleteServiceOffering(serviceOffering) {
        this.serviceOfferingToDelete = serviceOffering
        this.serviceOfferingDeleteDialog = true
      },
      onDeleteServiceOfferingConfirmed () {
        ServiceManagementClient.serviceOfferingsApi.deleteServiceOffering(this.serviceOfferingToDelete.id).then(response => {
          this.$toast.info(`Successfully delete service offering '${this.serviceOfferingToDelete.name}'`)
          this.serviceOfferingDeleteDialog = false
          this.serviceOfferingsStore.getServiceOfferings();
          ServiceManagementClient.serviceOfferingsApi.getServiceOfferings(false, this.selectedServiceVendor.id).then(
              response => {
                this.serviceOfferingsOfVendor = response.data
                this.serviceOfferingsOfVendorLoaded = true

              },
          ).catch(logRequestError)
        }).catch(logRequestError);
      },

      onDeleteServiceOfferingVersion(serviceOfferingVersion) {
        this.serviceOfferingVersionToDelete = serviceOfferingVersion
        this.serviceOfferingVersionDeleteDialog = true
      },
      onDeleteServiceOfferingVersionConfirmed () {
        ServiceManagementClient.serviceOfferingVersionsApi.deleteServiceOfferingVersion(
            this.serviceOfferingVersionToDelete.serviceOfferingId, this.serviceOfferingVersionToDelete.id)
        .then(response => {
          this.$toast.info(`Successfully delete service offering version '${this.serviceOfferingVersionToDelete.version}'`)
          this.serviceOfferingVersionDeleteDialog = false
          this.serviceOfferingVersionToDelete = undefined
          this.$options.serviceOfferingTableInterface.updateExpanded()
        }).catch(logRequestError);
      },

      onServiceVendorSelected () {
        this.serviceOfferingsOfVendor = null

        this.loadRepositories()

        ServiceManagementClient.serviceOfferingsApi.getServiceOfferings(false, this.selectedServiceVendor.id).then(
          response => {
            this.serviceOfferingsOfVendor = response.data
            this.serviceOfferingsOfVendorLoaded = true
          },
        ).catch(logRequestError)
      },

      onServiceRepositoryCreateDialogConfirmed (repository) {
        let api;
        if (repository.id === null || repository.id === ''){
          api = ServiceManagementClient.serviceRepositoriesApi.createRepository(this.selectedServiceVendor.id, repository);
        }else{
          api = ServiceManagementClient.serviceRepositoriesApi.createOrUpdateRepository(this.selectedServiceVendor.id, repository.id, repository);
        }

        api.then(response => {
          this.loadRepositories()
          this.$toast.info(`Successfully created repository '${repository.address}'`)
          this.serviceRepositoryCreateDialog = false
        })
          .catch((error) => {
            this.$toast.error(`Failed to created repository '${repository.address}'`)
            logRequestError(error)
          })
      },
      onDeleteRepositoryClicked (repository) {
        ServiceManagementClient.serviceRepositoriesApi.deleteRepository(this.selectedServiceVendor.id, repository.id).then(() => {
          this.loadRepositories()
          this.$toast.info(`Successfully removed repository '${repository.address}'`)
        }).catch(logRequestError)
      },

      loadRepositories () {
        ServiceManagementClient.serviceRepositoriesApi.getRepositories(this.selectedServiceVendor.id).then(
          response => {
            this.repositoriesOfVendor = response.data
          },
        ).catch(logRequestError)
      },

      onEditServiceVendorDialogConfirmed (updatedServiceVendor) {
        this.showEditServiceVendorDialog = false
        this.selectedServiceVendor = updatedServiceVendor
      },
    },
  }
</script>

<style lang="scss">
#no-background-hover::before {
  background-color: transparent !important;
}
</style>
