<template>
  <v-dialog
    v-model="active"
    width="600"
    @click:outside="$emit('canceled')"
  >
    <template #default="{}">
      <v-card>
        <v-toolbar
          color="primary"
          theme="dark"
        >
          {{ title }}
        </v-toolbar>
        <v-card-text>
          <v-form>
            <v-row
              v-for="param in params"
              :key="param.name"
            >
              <capability-params-dialog-input-field
                :param="param"
                @updateParamMap="updateParamMap"
              />
            </v-row>
          </v-form>
        </v-card-text>
        <v-card-actions class="justify-center">
          <v-btn
            color="error"
            class="mx-0"
            @click.native="$emit('canceled')"
          >
            Back
          </v-btn>
          <v-spacer />
          <v-btn
            color="info"
            class="mx-0"
            @click="$emit('install', paramMap)"
          >
            Install
          </v-btn>
        </v-card-actions>
      </v-card>
    </template>
  </v-dialog>
</template>
<script>
  import { capabilityUtilsMixin } from '@/utils/capabilityUtils'
  import CapabilityParamsDialogInputField from "@/components/resources/dialogs/CapabilityParamsDialogInputField.vue";
  import {toRef} from "vue";
 export default {
   name: "CapabilityParamsDialog",
   components: {CapabilityParamsDialogInputField},
   mixins: [capabilityUtilsMixin],
   props: ['showDialog', 'resourceId', 'capabilityId', 'skipInstall'],
   setup(props){
     const active = toRef(props, 'showDialog')
     return{
       active
     }
   },
   data() {
     return {
       title: "Set Configuration Parameter of Capability",
       paramMap: {}
     }
   },
   computed: {
     capability() {
       return this.getCapability(this.capabilityId)
     },
     params() {
       return this.getParamsOfInstallAction(this.capabilityId)
     }
   },
   watch: {
     capabilityId() {
       this.initParamMap()
     }
   },
   methods: {
     initParamMap() {
       this.params.forEach((p) => {
         this.paramMap[p.name] = p.defaultValue
       })
     },
     updateParamMap(key, value) {
       this.paramMap[key] = value
     }
   }
 }
</script>