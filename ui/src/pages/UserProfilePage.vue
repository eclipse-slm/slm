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
          <user-profile-details
            :user-id="userId"
            :user-name="userName"
            :user-info="userInfo"
          />
          <user-credentials/>

    </v-container>
  </div>
</template>

<script>

import ApiState from '@/api/apiState'
import {useUserStore} from "@/stores/userStore";
import UserProfileDetails from '@/components/user/UserProfileDetails.vue';
import UserCredentials from "@/components/user/UserCredentials.vue";

export default {
    setup(){
      const userStore = useUserStore();

      return {userStore};
    },
    computed: {
      apiStateUser () { return this.userStore.apiState },
      apiStateLoaded () { return this.apiStateUser === ApiState.LOADED },
      apiStateLoading () {
        if (this.apiStateUser === ApiState.INIT) { this.userStore.updateStore() }
        return this.apiStateUser === ApiState.LOADING || this.apiStateUser === ApiState.INIT
      },
      apiStateError () { return this.apiStateUser === ApiState.ERROR },
      userId() { return this.userStore.userId },
      userName () { return this.userStore.userName },
      userInfo () { return this.userStore.userInfo },
      userRoles () { return this.userStore.userRoles },
     },
  components: {UserCredentials, UserProfileDetails },
   }
</script>
