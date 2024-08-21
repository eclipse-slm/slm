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

import ServiceVendorsRestApi from '@/api/service-management/serviceVendorsRestApi'
import UsersRestApi from '@/api/service-management/usersRestApi'
import OverviewHeading from "@/components/base/OverviewHeading.vue";
import {app} from "@/main";
import {useUserStore} from "@/stores/userStore";
import {useServicesStore} from "@/stores/servicesStore";
import {storeToRefs} from "pinia";

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
      const servicesStore = useServicesStore();
      const {serviceOfferingCategoryNameById, serviceVendorById} = storeToRefs(servicesStore)
      return {userStore, servicesStore, serviceOfferingCategoryNameById, serviceVendorById}
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
          app.config.globalProperties.$toast.warning('Last developer of service vendor cannot be deleted')
        } else {
          ServiceVendorsRestApi.removeDeveloperFromServiceVendor(this.serviceVendor.id, deletedDeveloper.id).then(() => {
            app.config.globalProperties.$toast.info(`Successfully removed developer '${deletedDeveloper.username}'`)
            this.loadDevelopersOfServiceVendor()
          })
            .catch(() => {
              app.config.globalProperties.$toast.error(`Failed to remove developer '${deletedDeveloper.username}'`)
            })
        }
      },
      onSaveDevelopersClicked () {
        console.log(this.addedDevelopers)
        this.addedDevelopers.forEach(developer => {
          ServiceVendorsRestApi.addDeveloperToServiceVendor(this.serviceVendor.id, developer.id).then(() => {
            this.developersOfServiceVendor.push(developer)
            app.config.globalProperties.$toast.info(`Successfully added developer '${developer.username}'`)
            app.config.globalProperties.$keycloak.keycloak.updateToken(100000) // Force refresh of token
          })
        })
        this.addedDevelopers = []
      },

      loadDevelopersOfServiceVendor () {
        ServiceVendorsRestApi.getDevelopersOfServiceVendor(this.serviceVendor.id).then(
          developers => {
            this.developersOfServiceVendor = developers
          },
        )
      },
      loadUsers () {
        UsersRestApi.getUsers().then(
          users => {
            this.users = users
          },
        )
      },
    },
  }
</script>

<style>
</style>
