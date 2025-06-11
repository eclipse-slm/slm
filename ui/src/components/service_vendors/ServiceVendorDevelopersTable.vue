<template>
  <v-container fluid>
    <div>
      <base-material-card color="secondary">
        <template #heading>
          <overview-heading :text="'Developers of \''+serviceVendor.name+'\''" />
        </template>

        <v-progress-circular
          v-if="developersOfServiceVendor == null"
          :size="70"
          :width="7"
          color="primary"
          indeterminate
          justify="center"
        />

        <v-autocomplete
          v-model="addedDevelopers"
          :items="developersAvailableForSharing"
          label="Add developer"
          item-title="username"
          return-object
          multiple
          chips
          closable-chips
        />

        <v-divider />

        <v-data-table
          v-if="developersOfServiceVendor != null && developersOfServiceVendor.length > 0"
          :headers="tableHeaders"
          item-key="id"
          :items="developersOfServiceVendor"
        >
          <template
            #body="{ items }"
          >
            <tbody
              v-for="developer in items"
              :key="developer.id"
            >
              <tr>
                <td>{{ developer.username }}</td>
                <td>{{ developer.firstName }}</td>
                <td>{{ developer.lastName }}</td>
                <td>{{ developer.email }}</td>
                <td>
                  <v-btn
                    class="ma-1"
                    color="error"
                    size="small"
                    @click="onDeleteDeveloperClicked(developer)"
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
          class="ma-1"
          color="secondary"
          align="right"
          @click="onSaveDevelopersClicked()"
        >
          <v-icon
            density="compact"
            size="small"
            class="mr-2"
          >
            mdi-check
          </v-icon>
          Save
        </v-btn>
      </base-material-card>
    </div>
  </v-container>
</template>

<script>

import OverviewHeading from "@/components/base/OverviewHeading.vue";
import {useUserStore} from "@/stores/userStore";
import {useServiceOfferingsStore} from "@/stores/serviceOfferingsStore";
import {storeToRefs} from "pinia";
import ServiceManagementClient from "@/api/service-management/service-management-client";
import logRequestError from "@/api/restApiHelper";

export default {
    name: 'ServiceVendorDevelopersTable',
    components: {OverviewHeading},
    props: {
      serviceVendor: {
        type: Object,
        default: null
      }
    },
    setup(){
      const userStore = useUserStore();
      const serviceOfferingsStore = useServiceOfferingsStore();
      const {serviceOfferingCategoryNameById, serviceVendorById} = storeToRefs(serviceOfferingsStore)
      return {userStore, serviceOfferingsStore, serviceOfferingCategoryNameById, serviceVendorById}
    },
    data () {
      return {
        availableDevelopers: null,
        users: [],
        addedDevelopers: [],
        developersOfServiceVendor: [],
      }
    },
    computed: {
      userId() {
        return this.userStore.userId
      },

      tableHeaders () {
        return [
          { title: 'Username', value: 'username', sortable: true },
          { title: 'Firstname', value: 'firstName', sortable: true },
          { title: 'Lastname', value: 'lastName', sortable: true },
          { title: 'Mail', value: 'email', sortable: true },
          { title: 'Actions', value: 'developerActions', sortable: false },
        ]
      },
      developersAvailableForSharing () {
        const usersToRemove = []
        for (const developer of this.developersOfServiceVendor) {
          usersToRemove.push(developer.id)
        }
        return this.users.filter(function (developer) {
          return usersToRemove.indexOf(developer.id) === -1
        })
      },
    },
    watch: {
      serviceVendor: {
        immediate: true,
        handler (newValue, oldValue) {
          if (this.serviceVendor != null) {
            this.loadDevelopersOfServiceVendor()
            this.loadUsers()
          }
        },
      },
    },
    methods: {
      onDeleteDeveloperClicked (deletedDeveloper) {
        if (this.developersOfServiceVendor.length === 1) {
          this.$toast.warning('Last developer of service vendor cannot be deleted')
        } else {
          ServiceManagementClient.serviceVendorsApi.removeDeveloperFromServiceVendor(this.serviceVendor.id, deletedDeveloper.id).then(() => {
            this.$toast.info(`Successfully removed developer '${deletedDeveloper.username}'`)
            this.loadDevelopersOfServiceVendor()
          })
            .catch(() => {
              this.$toast.error(`Failed to remove developer '${deletedDeveloper.username}'`)
            })
        }
      },
      onSaveDevelopersClicked () {
        this.addedDevelopers.forEach(developer => {
          ServiceManagementClient.serviceVendorsApi.addDeveloperToServiceVendor(this.serviceVendor.id, developer.id).then(() => {
            this.developersOfServiceVendor.push(developer)
            this.$toast.info(`Successfully added developer '${developer.username}'`)
          }).catch(logRequestError)
        })
        this.addedDevelopers = []
      },

      loadDevelopersOfServiceVendor () {
        ServiceManagementClient.serviceVendorsApi.getDevelopersOfServiceVendor(this.serviceVendor.id).then(
          response => {
            this.developersOfServiceVendor = response.data
          },
        ).catch(logRequestError)
      },
      loadUsers () {
        ServiceManagementClient.usersApi.getUsers().then(
          response => {
            this.users = response.data
          },
        ).catch(logRequestError);
      },
    },
  }
</script>

<style>
</style>
