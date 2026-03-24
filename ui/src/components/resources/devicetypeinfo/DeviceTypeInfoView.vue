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
          {{ resourceType.typeName }}
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

            <!-- Firmware -->
            <v-tab :value="2">
              <v-icon>mdi-cellphone-arrow-down</v-icon>
              Firmware
            </v-tab>
          </v-tabs>

          <v-tabs-window v-model="tab">
            <!-- Common -->
            <v-tabs-window-item
              :value="1"
            >
              <DeviceTypeInfoCommonView :resource-type="resourceType" />
            </v-tabs-window-item>

            <!-- Firmware -->
            <v-tabs-window-item
              :value="2"
            >
              <DeviceTypeInfoFirmwareView
                :resource-type="resourceType"
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
import {ref, watch} from 'vue';
import DeviceTypeInfoCommonView from "@/components/resources/devicetypeinfo/DeviceTypeInfoCommonView.vue";
import DeviceTypeInfoFirmwareView from "@/components/resources/devicetypeinfo/DeviceTypeInfoFirmwareView.vue";

const emit = defineEmits(['closed']);

const props = defineProps({
  resourceType: {
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

watch(() => props.resourceType, () => {
  show.value = props.resourceType !== null;

  switch (props.section) {
    case 'common':
      tab.value = 1;
      break;
    case 'firmware':
      tab.value = 2;
      break;
  }
});

const onCloseButtonClicked = () => {
  emit('closed');
};
</script>
