<template>
  <ConfirmDialog
    :show="show"
    title="Set configuration parameters of capability"
    confirm-button-label="Install"
    cancel-button-label="Cancel"
    @canceled="$emit('canceled')"
    @confirmed="$emit('confirmed', parameterMap)"
  >
    <template #content>
      <div class="mt-4">
        <v-form>
          <v-row
            v-for="param in params"
            :key="param.name"
          >
            <capability-params-dialog-input-field
              :parameter="param"
              @parameter-value-changed="onParameterValueChanged"
            />
          </v-row>
        </v-form>
      </div>
    </template>
  </ConfirmDialog>


<!--  <v-dialog-->
<!--    v-model="visible"-->
<!--    width="600"-->
<!--    @click:outside="$emit('canceled')"-->
<!--  >-->
<!--    <template #default="{}">-->
<!--      <v-card>-->
<!--        <v-toolbar-->
<!--          color="primary"-->
<!--          theme="dark"-->
<!--        >-->
<!--          Set Configuration Parameter of Capability-->
<!--        </v-toolbar>-->
<!--        <v-card-text>-->
<!--          -->
<!--        </v-card-text>-->
<!--        <v-card-actions class="justify-center">-->
<!--          <v-btn-->
<!--            variant="elevated"-->
<!--            color="error"-->
<!--            class="mx-0"-->
<!--            @click.native="$emit('canceled')"-->
<!--          >-->
<!--            Back-->
<!--          </v-btn>-->
<!--          <v-spacer />-->
<!--          <v-btn-->
<!--            variant="elevated"-->
<!--            color="info"-->
<!--            class="mx-0"-->
<!--            @click="$emit('confirmed', parameterMap)"-->
<!--          >-->
<!--            Install-->
<!--          </v-btn>-->
<!--        </v-card-actions>-->
<!--      </v-card>-->
<!--    </template>-->
<!--  </v-dialog>-->
</template>

<script setup>
import {toRef, ref, watch, computed, onMounted} from 'vue';
import { useCapabilityUtils } from '@/utils/capabilityUtils';
import CapabilityParamsDialogInputField from "@/components/resources/capabilities/CapabilityParamsDialogInputField.vue";
import ConfirmDialog from "@/components/base/ConfirmDialog.vue";

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  resourceId: {
    type: String,
    default: null
  },
  capabilityId: {
    type: String,
    default: null
  },
  skipInstall: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['canceled', 'confirmed']);

const visible = toRef(props, 'showDialog');
const parameterMap = ref({});

const capabilityUtils = useCapabilityUtils();

const params = computed(() => capabilityUtils.getParamsOfInstallAction(props.capabilityId));

const initParamMap = () => {
  params.value.forEach((p) => {
    parameterMap.value[p.name] = p.defaultValue;
  });
};

const onParameterValueChanged = (key, value) => {
  parameterMap.value[key] = value;
};

// Initialize parameter map when dialog is shown
watch(visible, (newValue, oldValue) => {
  if (newValue === true) {
    initParamMap();
  }
});

</script>