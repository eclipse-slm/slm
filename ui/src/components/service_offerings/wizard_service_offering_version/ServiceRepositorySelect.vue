<template>
  <v-row>
    <v-col>
      <v-row>
        <v-col cols="3">
          <v-subheader>
            {{ label }}
            <v-tooltip bottom>
              <template #activator="{ on, attrs }">
                <v-icon
                  class="mx-3"
                  color="secondary"
                  dark
                  v-bind="attrs"
                  @click.stop="ImageDialog = true"
                  v-on="on"
                >
                  mdi-information
                </v-icon>
              </template>
              <span>Credentials for Docker Registry used during service deployment.</span>
            </v-tooltip>
          </v-subheader>
        </v-col>
        <v-col cols="9">
          <v-select
            :items="availableRepositories"
            item-value="id"
            :multiple="multiple"
            :item-text="item => `${item.address} (Username: ${item.username})`"
            :chips="multiple"
            :deletable-chips="multiple"
            @change="onSelectedRepositoriesChanged"
          />
        </v-col>
      </v-row>
    </v-col>
  </v-row>
</template>

<script>
  import ServiceVendorsRestApi from '@/api/service-management/serviceVendorsRestApi'

  export default {
    name: 'ServiceRepositorySelect',
    props: {
      value: {
        type: Array,
      },
      serviceVendorId: {
        type: String,
        required: true,
      },
      label: {
        type: String,
        default: 'Service Repository Credentials',
      },
      multiple: {
        type: Boolean,
        default: false,
      },
    },
    data () {
      return {
        availableRepositories: [],
      }
    },
    created () {
      this.loadRepositories()
    },
    methods: {
      onSelectedRepositoriesChanged (selectedRepositories) {
        if (!Array.isArray(selectedRepositories)) {
          selectedRepositories = [selectedRepositories]
        }
        this.$emit('input', selectedRepositories)
      },
      loadRepositories () {
        ServiceVendorsRestApi.getRepositoriesOfServiceVendor(this.serviceVendorId).then(
          repositories => {
            this.availableRepositories = repositories
          },
        )
      },
    },
  }
</script>
