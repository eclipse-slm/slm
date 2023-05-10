<template>
  <v-dialog
    v-model="show"
    width="400"
    @click:outside="$emit('canceled')"
  >
    <template>
      <v-card v-if="show">
        <v-toolbar
          color="primary"
          dark
        >
          {{ title }}
        </v-toolbar>
        <v-card-text>
          {{ text }}
        </v-card-text>
        <v-card-actions class="justify-center">
          <v-spacer />

          <v-btn
            id="confirm-dialog-button-yes"
            color="error"
            @click="$emit('confirmed')"
          >
            Yes
          </v-btn>

          <v-btn
            id="confirm-dialog-button-no"
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
    name: 'ConfirmDialog',
    props: ['show', 'title', 'text'],
  }
</script>
