<template>
  <v-dialog
    v-model="active"
    width="400"
    @click:outside="$emit('canceled')"
  >
    <template v-if="serviceCategoryUpdate != null">
      <v-card>
        <v-toolbar
          color="primary"
          theme="dark"
        >
          Create new service category
        </v-toolbar>
        <v-card-text>
          <v-container class="pa-8">
            <v-text-field
              v-model="serviceCategoryUpdate.name"
              label="Name"
              required
              prepend-icon="mdi-account"
            />
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

  import ServiceOfferingsRestApi from '@/api/service-management/serviceOfferingsRestApi'
  import Vue, {toRef} from 'vue'

  export default {
    name: 'ServiceCategoryCreateOrEditDialog',
    props: ['show', 'editMode', 'serviceCategory'],
    data () {
      return {
      }
    },
    computed: {
      serviceCategoryUpdate () {
        return JSON.parse(JSON.stringify(this.serviceCategory))
      },
    },
    methods: {
      onConfirmedClicked () {
        let apiCall
        if (this.editMode) {
          apiCall = ServiceOfferingsRestApi.createOrUpdateServiceCategoryWithId(this.serviceCategoryUpdate)
        } else {
          apiCall = ServiceOfferingsRestApi.createServiceCategory(this.serviceCategoryUpdate)
        }

        apiCall.then(() => {
          if (this.editMode) {
            Vue.$toast.info('Service category successfully updated')
          } else {
            Vue.$toast.info('Service category successfully created')
          }
          this.$store.dispatch('getServiceOfferingCategories')
          this.$emit('confirmed', this.serviceVendorUpdate)
        }).catch(exception => {
          Vue.$toast.error('Failed to create service category')
          console.log('Service category creation failed: ' + exception.response.data.message)
          console.log(exception)
        })
      },
    },
    setup(props){
      const active = toRef(props, 'show')
      return{
        active
      }
    }
  }
</script>
