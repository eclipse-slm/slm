<template>
  <v-dialog
    v-model="active"
    width="400"
    @click:outside="$emit('canceled')"
  >
    <template #default="{isActive}">
      <v-card v-if="isActive">
        <v-toolbar
          color="primary"
          theme="dark"
        >
          {{ title }}
        </v-toolbar>
        <v-card-text class="my-4">
          {{ text }}
        </v-card-text>
        <v-card-actions class="justify-center">
          <v-spacer />

          <v-btn
            id="confirm-dialog-button-yes"
            variant="elevated"
            color="error"
            @click="$emit('confirmed')"
          >
            Yes
          </v-btn>

          <v-btn
            id="confirm-dialog-button-no"
            variant="elevated"
            color="info"
            @click.native="$emit('canceled')"
          >
            No
          </v-btn>
        </v-card-actions>
      </v-card>
    </template>
  </v-dialog>
</template>

<script>
import {toRef} from "vue";

export default {
    name: 'ConfirmDialog',
    props: {
      show: {
        type: Boolean,
        default: false
      },
      title: {
        type: String,
        default: ""
      },
      text: {
        type: String,
        default: ""
      }
    },
    setup(props){
      const active = toRef(props, 'show')
      return{
        active
      }
    }
  }
</script>
