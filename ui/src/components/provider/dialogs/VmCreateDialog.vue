<template>
  <v-dialog
    v-model="show"
    max-width="800px"
    @click:outside="closeDialog"
  >
    <template>
      <v-toolbar
        color="primary"
        dark
      >
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
            cols="1"
          >
            <v-btn
              icon
              @click="closeDialog"
            >
              <v-icon>mdi-close</v-icon>
            </v-btn>
          </v-col>
        </v-row>
      </v-toolbar>
      <v-card>
        <v-container class="pa-8">
          <vm-create-dialog-form :parameter="parameter" />
        </v-container>
        <v-card-actions>
          <v-btn
            text
            @click="closeDialog"
          >
            Cancel
          </v-btn>
          <v-spacer />
          <v-btn
            text
            :color="$vuetify.theme.themes.light.secondary"
          >
            Create
          </v-btn>
        </v-card-actions>
      </v-card>
    </template>
  </v-dialog>
</template>

<script>
import {mapGetters} from "vuex";
import VmCreateDialogForm from "@/components/provider/dialogs/VmCreateDialogForm.vue";

export default {
  name: 'VmCreateDialog',
  components: {VmCreateDialogForm},
  props: ['show','virtualResourceProvider'],
  data () {
    return {
      title: 'Create VM'
    }
  },
  computed: {
    parameter() {
      if(this.virtualResourceProvider)
        return this.virtualResourceProvider.capabilityService.capability.actions.CREATE_VM.parameter
      else
        return []
    }
  },
  methods: {
    closeDialog () {
      this.$emit('canceled')
    }
  },
}
</script>