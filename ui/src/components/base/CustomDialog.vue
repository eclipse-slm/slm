<template>
  <v-dialog
    v-model="show"
    :width="width"
    @click:outside="$emit('canceled')"
  >
    <template>
      <v-card v-if="show">
        <v-toolbar
          color="primary"
          dark
        >
          <slot name="header">
            <v-row
              align="center"
              justify="center"
            >
              <v-col cols="11">
                <div class="font-weight-light">
                  {{ title }}
                </div>
              </v-col>
              <v-spacer />
              <v-col
                v-if="closeButtonVisible"
                cols="1"
              >
                <v-btn
                  icon
                  @click="$emit('canceled')"
                >
                  <v-icon>mdi-close</v-icon>
                </v-btn>
              </v-col>
            </v-row>
          </slot>
        </v-toolbar>
        <v-card-text>
          <slot name="content">
            {{ text }}
          </slot>
        </v-card-text>
        <v-card-actions class="justify-center">
          <slot name="actions">
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
          </slot>
        </v-card-actions>
      </v-card>
    </template>
  </v-dialog>
</template>

<script>
  export default {
    name: 'CustomDialog',
    props: {
      show: {
        default: false,
        type: Boolean
      },
      closeButtonVisible: {
        default: true,
        type: Boolean
      },
      title: {
        default: 'Custom dialog title',
        type: String
      },
      text: {
        default: 'Custom dialog text',
        type: String
      },
      width: {
        default: '400',
        type: String
      }
    }
  }
</script>
