<template>
  <v-dialog
    v-model="show"
    width="400"
    @click:outside="$emit('canceled')"
  >
    <template v-if="serviceVendorUpdate != null">
      <v-card>
        <v-toolbar
          color="primary"
          dark
        >
          Create new service vendor
        </v-toolbar>
        <v-card-text>
          <v-container class="pa-8">
            <v-text-field
              v-model="serviceVendorUpdate.name"
              label="Name"
              required
              prepend-icon="mdi-account"
            />
            <v-text-field
              v-model="serviceVendorUpdate.description"
              label="Description"
              required
              prepend-icon="mdi-card-text-outline"
            />

            <v-file-input
              v-model="uploadedServiceVendorLogo"
              prepend-icon="mdi-file-image"
              label="Logo"
              auto-grow
              dense
              outlined
              @change="loadServiceVendorLogo"
            />

            <v-row>
              <v-avatar
                ref="serviceVendorAvatar"
                size="64"
                class="mx-auto elevation-6"
              >
                <v-img
                  ref="serviceVendorLogo"
                  accept="image/*"
                  :src="getImageUrl(serviceVendorUpdate.logo)"
                />
              </v-avatar>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions class="justify-center">
          <v-btn
            color="error"
            @click.native="$emit('canceled')"
          >
            Cancel
          </v-btn>
          <v-spacer />
          <v-btn
            color="info"
            @click="onConfirmedClicked"
          >
            <div v-if="editMode">
              Update
            </div>
            <div v-else>
              Create
            </div>
          </v-btn>
        </v-card-actions>
      </v-card>
    </template>
  </v-dialog>
</template>

<script>

  import ServiceVendorsRestApi from '@/api/service-management/serviceVendorsRestApi'
  import Vue from 'vue'
  import getImageUrl from '@/utils/imageUtil'

  export default {
    name: 'ServiceVendorCreateOrEditDialog',
    props: ['show', 'editMode', 'serviceVendor'],
    data () {
      return {
        uploadedServiceVendorLogo: null,
      }
    },
    computed: {
      serviceVendorUpdate () {
        return JSON.parse(JSON.stringify(this.serviceVendor))
      },
    },
    methods: {
      getImageUrl (imageData) { return getImageUrl(imageData) },
      loadServiceVendorLogo (files) {
        if (!this.uploadedServiceVendorLogo) {
          console.log('No File Chosen')
        } else {
          const reader = new FileReader()
          reader.readAsDataURL(this.uploadedServiceVendorLogo)
          reader.onload = () => {
            this.serviceVendorUpdate.logo = reader.result
            this.$refs.serviceVendorLogo.$forceUpdate()
            this.$refs.serviceVendorAvatar.$forceUpdate()
          }
        }
      },

      onConfirmedClicked () {
        if (this.serviceVendorUpdate.logo != null && this.serviceVendorUpdate.logo.includes('image/')) {
          this.serviceVendorUpdate.logo =
            this.serviceVendorUpdate.logo.slice(this.serviceVendorUpdate.logo.indexOf(',') + 1, this.serviceVendorUpdate.logo.length)
        }

        let apiCall
        if (this.editMode) {
          apiCall = ServiceVendorsRestApi.createOrUpdateServiceVendorWithId(this.serviceVendorUpdate)
        } else {
          apiCall = ServiceVendorsRestApi.createServiceVendor(this.serviceVendorUpdate)
        }

        apiCall.then(() => {
          if (this.editMode) {
            Vue.$toast.info('Service vendor successfully updated')
          } else {
            Vue.$toast.info('Service vendor successfully created')
          }
          this.$store.dispatch('getServiceVendors')
          this.$emit('confirmed', this.serviceVendorUpdate)
        }).catch(exception => {
          Vue.$toast.error('Failed to create service vendor')
          console.log('Service vendor creation failed: ' + exception.response.data.message)
          console.log(exception)
        })
      },
    },
  }
</script>
