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
            variant="elevated"
            color="error"
            @click.native="$emit('canceled')"
          >
            Cancel
          </v-btn>
          <v-spacer />
          <v-btn
            variant="elevated"
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
  import {app} from "@/main";
  import {useServicesStore} from "@/stores/servicesStore";

  export default {
    name: 'ServiceCategoryCreateOrEditDialog',
    props: ['show', 'editMode', 'serviceCategory'],
    setup(props){
      const active = toRef(props, 'show')
      return{
        active
      }
    },
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
            app.config.globalProperties.$toast.info('Service category successfully updated')
          } else {
            app.config.globalProperties.$toast.info('Service category successfully created')
          }

          const serviceStore = useServicesStore();
          serviceStore.getServiceOfferingCategories();

          this.$emit('confirmed', this.serviceVendorUpdate)
        }).catch(exception => {
          app.config.globalProperties.$toast.error('Failed to create service category')
          console.log('Service category creation failed: ' + exception.response.data.message)
          console.log(exception)
        })
      },
    }
  }
</script>
