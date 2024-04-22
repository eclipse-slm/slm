<template>
  <div>
    <div
      v-if="apiStateLoading"
      class="text-center"
    >
      <v-progress-circular
        :size="70"
        :width="7"
        color="primary"
        indeterminate
      />
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
      <v-row justify="center">
        <v-col
          cols="12"
          md="8"
        >
          <base-material-card color="secondary">
            <template #heading>
              <div class="text-h3 font-weight-light">
                User Profile
              </div>
            </template>

            <v-form>
              <v-container class="py-0">
                <v-row>
                  <v-col
                    cols="12"
                    md="4"
                  >
                    <v-text-field
                      label="Id"
                      :readonly="true"
                      :model-value="userId"
                    />
                  </v-col>

                  <v-col
                    cols="12"
                    md="4"
                  >
                    <v-text-field
                      class="purple-input"
                      label="User Name"
                      :model-value="userName"
                      :readonly="true"
                    />
                  </v-col>

                  <v-col
                    cols="12"
                    md="4"
                  >
                    <v-text-field
                      label="Email Address"
                      class="purple-input"
                      :model-value="userInfo.email"
                      :readonly="true"
                    />
                  </v-col>

                  <v-col
                    cols="12"
                    md="6"
                  >
                    <v-text-field
                      label="First Name"
                      class="purple-input"
                      :model-value="userInfo.given_name"
                      :readonly="true"
                    />
                  </v-col>

                  <v-col
                    cols="12"
                    md="6"
                  >
                    <v-text-field
                      label="Last Name"
                      class="purple-input"
                      :model-value="userInfo.family_name"
                      :readonly="true"
                    />
                  </v-col>

                  <v-col
                    cols="12"
                    class="text-right"
                  />
                </v-row>
              </v-container>
            </v-form>
          </base-material-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script>
  import { mapGetters } from 'vuex'
  import ApiState from '@/api/apiState'

  export default {
    computed: {
      ...mapGetters([
        'apiStateUser',
        'userId',
        'userName',
        'userInfo',
        'userRoles',
      ]),
      apiStateLoaded () {
        return this.apiStateUser === ApiState.LOADED
      },
      apiStateLoading () {
        if (this.apiStateUser === ApiState.INIT) {
          this.$store.dispatch('getUserDetails')
        }
        return this.apiStateUser === ApiState.LOADING || this.apiStateUser === ApiState.INIT
      },
      apiStateError () {
        return this.apiStateUser === ApiState.ERROR
      },
    },
  }
</script>
