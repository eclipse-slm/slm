<template>
  <base-material-card color="secondary">
    <template #heading>
      <div class="text-h3 font-weight-light">
        User Profile
      </div>
    </template>

    <v-container>
      <v-row>
        <v-col cols="12" md="4">
          <v-text-field
            label="User ID"
            :readonly="true"
            :model-value="userIdComputed"
          />
        </v-col>

        <v-col cols="12" md="4">
          <v-text-field
            class="purple-input"
            label="User Name"
            :model-value="userNameComputed"
            :readonly="true"
          />
        </v-col>

        <v-col cols="12" md="4">
          <v-text-field
            label="Email Address"
            class="purple-input"
            :model-value="userInfoComputed.email"
            :readonly="true"
          />
        </v-col>

        <v-col cols="12" md="6">
          <v-text-field
            label="First Name"
            class="purple-input"
            :model-value="userInfoComputed.given_name"
            :readonly="true"
          />
        </v-col>

        <v-col cols="12" md="6">
          <v-text-field
            label="Last Name"
            class="purple-input"
            :model-value="userInfoComputed.family_name"
            :readonly="true"
          />
        </v-col>

        <v-col cols="12" class="text-right" />
      </v-row>
    </v-container>
  </base-material-card>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/userStore'

const props = defineProps({
  userId: { type: String, default: '' },
  userName: { type: String, default: '' },
  userInfo: { type: Object, default: () => ({}) }
})

const userStore = useUserStore();

const userIdComputed = computed(() => {
  return props.userId && props.userId.length > 0 ? props.userId : userStore.userId;
});

const userNameComputed = computed(() => {
  return props.userName && props.userName.length > 0 ? props.userName : userStore.userName;
});

const userInfoComputed = computed(() => {
  const pi = props.userInfo;
  if (pi && Object.keys(pi).length > 0) return pi;
  return userStore.userInfo ?? {};
});
</script>

<style scoped>
</style>
