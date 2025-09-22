<template>
  <v-dialog
    v-model="show"
    width="90%"
    height="90%"
    @click:outside="onCloseButtonClicked"
  >
    <template #default="{}">
      <v-card
        v-if="show"
        height="90%"
        min-height="90%"
      >
        <v-toolbar
          color="primary"
          theme="dark"
        >
          {{ resource.hostname }}
        </v-toolbar>

        <v-card-text>
          <v-tabs
            v-model="tab"
            color="secondary"
            stacked
          >
            <!-- Common -->
            <v-tab :value="1">
              <v-icon>mdi-information-outline</v-icon>
              Common
            </v-tab>

            <!-- Nameplate -->
            <v-tab :value="2">
              <v-icon>mdi-id-card</v-icon>
              Nameplate
            </v-tab>

            <!-- Hardware -->
            <v-tab :value="3">
              <v-icon>mdi-expansion-card</v-icon>
              Hardware
            </v-tab>

            <!-- Firmware -->
            <v-tab :value="4">
              <v-icon>mdi-cellphone-arrow-down</v-icon>
              Firmware
            </v-tab>

            <!-- Capabilities -->
            <v-tab :value="5">
              <v-icon>mdi-toolbox</v-icon>
              Capabilities
            </v-tab>

            <!-- Submodels -->
            <v-tab :value="6">
              <v-icon>mdi-adjust</v-icon>
              Submodels
            </v-tab>
          </v-tabs>

          <v-tabs-window v-model="tab">
            <!-- Common -->
            <v-tabs-window-item
              :value="1"
            >
              <DeviceInfoCommonView :resource-id="resource.id" />
            </v-tabs-window-item>

            <!-- Nameplate -->
            <v-tabs-window-item
              :value="2"
            >
              <DeviceInfoNameplateView :resource-id="resource.id" />
            </v-tabs-window-item>

            <!-- Hardware -->
            <v-tabs-window-item
              :value="3"
            >
              <DeviceInfoHardwareView
                :resource-id="resource.id"
              />
            </v-tabs-window-item>

            <!-- Firmware -->
            <v-tabs-window-item
              :value="4"
            >
              <DeviceInfoFirmwareView
                :resource-id="resource.id"
              />
            </v-tabs-window-item>

            <!-- Capabilities -->
            <v-tabs-window-item
                :value="5"
            >
              <DeviceInfoCapabilitiesView
                  :resource-id="resource.id"
              />
            </v-tabs-window-item>

            <!-- Submodels -->
            <v-tabs-window-item
              :value="6"
            >
              <DeviceInfoSubmodelsView
                :resource-id="resource.id"
              />
            </v-tabs-window-item>
          </v-tabs-window>
        </v-card-text>

        <v-card-actions class="justify-end">
          <v-btn
            variant="text"
            @click="onCloseButtonClicked"
          >
            Close
          </v-btn>
        </v-card-actions>
      </v-card>
    </template>
  </v-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import DeviceInfoSubmodelsView from "@/components/resources/deviceinfo/DeviceInfoSubmodelsView.vue";
import DeviceInfoNameplateView from "@/components/resources/deviceinfo/DeviceInfoNameplateView.vue";
import DeviceInfoHardwareView from "@/components/resources/deviceinfo/DeviceInfoHardwareView.vue";
import DeviceInfoCommonView from "@/components/resources/deviceinfo/DeviceInfoCommonView.vue";
import DeviceInfoFirmwareView from "@/components/resources/deviceinfo/DeviceInfoFirmwareView.vue";
import DeviceInfoCapabilitiesView from "@/components/resources/deviceinfo/DeviceInfoCapabilitiesView.vue";

const emit = defineEmits(['closed']);

const props = defineProps({
  resource: {
    type: Object,
    default: null,
  },
  section: {
    type: String,
    default: 'common',
  }
});


const tab = ref(null);
const show = ref(false);

watch(() => props.resource, () => {
  show.value = props.resource !== null;

  switch (props.section) {
    case 'common':
      tab.value = 1;
      break;
    case 'nameplate':
      tab.value = 2;
      break;
    case 'hardware':
      tab.value = 3;
      break;
    case 'firmware':
      tab.value = 4;
      break;
    case 'submodels':
      tab.value = 5;
      break;
    default:
      tab.value = 1;
  }
});

const onCloseButtonClicked = () => {
  emit('closed');
};
</script>
