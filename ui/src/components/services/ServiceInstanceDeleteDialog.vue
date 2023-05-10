<template>
  <v-dialog
    v-model="show"
    width="400"
    @click:outside="$emit('canceled')"
  >
    <template>
      <v-card v-if="service">
        <v-toolbar
          color="primary"
          dark
        >
          Delete Service Instance '{{ service.id }}'
        </v-toolbar>
        <v-card-text>
          Do your really want to delete this service instance?
        </v-card-text>
        <v-card-actions class="justify-center">
          <v-spacer />

          <v-btn
            color="error"
            @click="$emit('confirmed')"
          >
            Yes
          </v-btn>

          <v-btn
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
  export default {
    name: 'ServiceInstanceDeleteDialog',
    props: ['service', 'show'],
  }
</script>
