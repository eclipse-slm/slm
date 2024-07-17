<template>
  <v-dialog
    v-model="active"
    width="400"
    @click:outside="$emit('canceled')"
  >
    <template #default>
      <v-card v-if="service">
        <v-toolbar
          color="primary"
          theme="dark"
        >
          Delete Service Instance '{{ service.id }}'
        </v-toolbar>
        <v-card-text>
          Do your really want to delete this service instance?
        </v-card-text>
        <v-card-actions class="justify-center">
          <v-spacer />

          <v-btn
            variant="elevated"
            color="error"
            @click="$emit('confirmed')"
          >
            Yes
          </v-btn>

          <v-btn
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
    name: 'ServiceInstanceDeleteDialog',
    props: ['service', 'show'],
    setup(props){
      const active = toRef(props, 'show')
      return{
        active
      }
    }
  }
</script>
