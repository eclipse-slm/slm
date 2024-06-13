<template>
  <div>
    <div
      v-if="apiStateLoading"
      class="text-center"
    >
      <progress-circular />
    </div>
    <div v-if="apiStateError">
      Error
    </div>

    <v-container
      v-if="apiStateLoaded"
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
          >
          </v-select>
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
              :text="`Do you really want to delete service offering '${serviceOfferingToDelete.name}'?`"
              @canceled="serviceOfferingDeleteDialog = false"
              @confirmed="onDeleteServiceOfferingConfirmed"
            />

            <!-- Service Offering Version Delete Dialog !-->
            <confirm-dialog
              v-if="serviceOfferingVersionDeleteDialog"
              :show="serviceOfferingVersionDeleteDialog"
              title="Delete service offering version?"
              :text="`Do you really want to delete service offering version '${serviceOfferingVersionToDelete.version}'?`"
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
              <template
                #body="{ items }"
              >
                <tbody
                  v-for="repository in items"
                  :key="repository.id"
                >
                  <tr>
                    <td>{{ repository.id }}</td>
                    <td>{{ repository.address }}</td>
                    <td>{{ repository.username }}</td>
                    <td>
                      <v-text-field
                        label="Encrypted"
                        disabled
                      />
                    </td>
                    <td>{{ repository.type }}</td>
                    <td>
                      <v-btn
                        class="ma-1"
                        size="small"
                        variant="outlined"

                        color="red"
                        @click="onDeleteRepositoryClicked(repository)"
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
  import { mapGetters } from 'vuex'
  import ServiceVendorsRestApi from '@/api/service-management/serviceVendorsRestApi'
  import ServiceOfferingsRestApi from '@/api/service-management/serviceOfferingsRestApi'
  import UsersRestApi from '@/api/service-management/usersRestApi'
  import ApiState from '@/api/apiState'
  import Vue from 'vue'
  import ServiceRepositoryCreateDialog from '@/components/service_vendors/ServiceRepositoryCreateDialog'
  import ServiceVendorsDevelopersTable from '@/components/service_vendors/ServiceVendorDevelopersTable'
  import ServiceVendorCreateOrEditDialog from '@/components/service_vendors/ServiceVendorCreateOrEditDialog'
  import logRequestError from '@/api/restApiHelper'
  import getImageUrl from '@/utils/imageUtil'
  import ServiceOfferingTable from "@/components/service_offerings/ServiceOfferingTable";
  import ConfirmDialog from "@/components/base/ConfirmDialog";
  import ServiceOfferingVersionsRestApi from  "@/api/service-management/serviceOfferingVersionsRestApi";
  import ServiceOfferingCreateDialog from "@/components/service_offerings/dialogs/ServiceOfferingCreateDialog";
  import ProgressCircular from "@/components/base/ProgressCircular";
  import OverviewHeading from "@/components/base/OverviewHeading.vue";
  import {app} from "@/main";

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
    props: ['serviceVendorId'],
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
    created () {
      this.$store.dispatch('getServiceVendors').then(() => {
        UsersRestApi.getServiceVendorsOfDeveloper(this.userId).then(serviceVendorIdsOfDeveloper => {
          this.serviceVendorsOfDeveloper = []
          serviceVendorIdsOfDeveloper.forEach(serviceVendorId => {
            if (this.serviceVendorId != undefined) {
              if (this.serviceVendorId == serviceVendorId) {
                this.selectedServiceVendor = this.serviceVendorById(serviceVendorId)
                this.onServiceVendorSelected()
              }
            }
            this.serviceVendorsOfDeveloper.push(this.serviceVendorById(serviceVendorId))
          })
        })
      })
    },
    computed: {
      ...mapGetters([
        'themeColorMain',
        'apiStateServices',
        'userId',
        'serviceOfferingCategoryNameById',
        'serviceVendorById',
      ]),
      apiStateLoaded () {
        return this.apiStateServices.serviceOfferingCategories === ApiState.LOADED
      },
      apiStateLoading () {
        if (this.apiStateServices.serviceOfferingCategories === ApiState.INIT) {
          this.$store.dispatch('getServiceOfferingCategories')
        }
        return this.apiStateServices.serviceOfferingCategories === ApiState.LOADING || this.apiStateServices.serviceOfferingCategories === ApiState.INIT
      },
      apiStateError () {
        return this.apiStateServices.serviceOfferingCategories === ApiState.ERROR
      },
      RepositoriesTableHeaders () {
        return [
          { text: 'Id', value: 'id', sortable: false },
          { text: 'Address', value: 'address', sortable: true },
          { text: 'Username', value: 'username', sortable: false },
          { text: 'Password', value: 'password', sortable: false },
          { text: 'Type', value: 'type', sortable: true },
        ]
      },
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
        ServiceOfferingsRestApi.deleteServiceOffering(this.serviceOfferingToDelete.id).then(response => {
          app.config.globalProperties.$toast.info(`Successfully delete service offering '${this.serviceOfferingToDelete.name}'`)
          this.serviceOfferingDeleteDialog = false
          this.$store.dispatch('getServiceOfferings')
          ServiceOfferingsRestApi.getOfferings(false, this.selectedServiceVendor.id).then(
              serviceOfferingsOfVendor => {
                this.serviceOfferingsOfVendor = serviceOfferingsOfVendor
                this.serviceOfferingsOfVendorLoaded = true

              },
          )
        })
      },

      onDeleteServiceOfferingVersion(serviceOfferingVersion) {
        this.serviceOfferingVersionToDelete = serviceOfferingVersion
        this.serviceOfferingVersionDeleteDialog = true
      },
      onDeleteServiceOfferingVersionConfirmed () {
        ServiceOfferingVersionsRestApi.deleteServiceOfferingVersion(
            this.serviceOfferingVersionToDelete.serviceOfferingId, this.serviceOfferingVersionToDelete.id)
        .then(response => {
          app.config.globalProperties.$toast.info(`Successfully delete service offering version '${this.serviceOfferingVersionToDelete.version}'`)
          this.serviceOfferingVersionDeleteDialog = false
          this.serviceOfferingVersionToDelete = undefined
          this.$options.serviceOfferingTableInterface.updateExpanded()
        })
      },

      onServiceVendorSelected () {
        this.serviceOfferingsOfVendor = null

        this.loadRepositories()

        ServiceOfferingsRestApi.getOfferings(false, this.selectedServiceVendor.id).then(
          serviceOfferingsOfVendor => {
            this.serviceOfferingsOfVendor = serviceOfferingsOfVendor
            this.serviceOfferingsOfVendorLoaded = true
          },
        )
      },

      onServiceRepositoryCreateDialogConfirmed (repository) {
        ServiceVendorsRestApi.addRepositoryToServiceVendor(this.selectedServiceVendor.id, repository).then(response => {
          this.loadRepositories()
          app.config.globalProperties.$toast.info(`Successfully created repository '${repository.address}'`)
          this.serviceRepositoryCreateDialog = false
        })
          .catch((error) => {
            app.config.globalProperties.$toast.error(`Failed to created repository '${repository.address}'`)
            logRequestError(error)
          })
      },
      onDeleteRepositoryClicked (repository) {
        ServiceVendorsRestApi.deleteRepositoryOfServiceVendor(this.selectedServiceVendor.id, repository.id).then(() => {
          this.loadRepositories()
          app.config.globalProperties.$toast.info(`Successfully removed repository '${repository.address}'`)
        })
      },

      loadRepositories () {
        ServiceVendorsRestApi.getRepositoriesOfServiceVendor(this.selectedServiceVendor.id).then(
          repositories => {
            this.repositoriesOfVendor = repositories
          },
        )
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
