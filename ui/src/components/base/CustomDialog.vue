<script setup>
import {toRef, ref, watch} from 'vue';

const emit = defineEmits(['canceled']);

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  closeButtonVisible: {
    type: Boolean,
    default: true
  },
  title: {
    type: String,
    default: 'Custom dialog title'
  },
  text: {
    type: String,
    default: 'Custom dialog text'
  },
  width: {
    type: String,
    default: '400'
  },
  hideActions: {
    type: Boolean,
    default: false
  }
});

const dialogActive = ref(Boolean(props.show))
const showProp = toRef(props,'show'); // react to prop

watch(showProp, (value) => {
  dialogActive.value = Boolean(value);
}, { immediate: true });

// Esc and outside click close the v-dialog by setting its v-model to false; tell the parent
// (which still has show=true) exactly once. When the parent closes via show=false, no emit.
watch(dialogActive, (active) => {
  if (!active && props.show) {
    emit('canceled');
  }
});
</script>

<template>
  <v-dialog
      v-model="dialogActive"
      :width="width"
  >
    <template #default="{}">
      <v-card
        v-if="dialogActive"
        class="custom-dialog-card"
      >
        <v-toolbar color="primary" theme="dark">
          <slot name="header">
            <div class="dialog-toolbar">
              <div class="font-weight-light">
                {{ title }}
              </div>
              <v-btn
                  v-if="closeButtonVisible"
                  @click="emit('canceled')"
                  icon="mdi-close"
                  class="close-btn"
              />
            </div>
          </slot>
        </v-toolbar>
        <v-card-text class="custom-dialog-content mt-8 mx-4">
            <slot name="content">
              {{ text }}
            </slot>
        </v-card-text>
        <v-card-actions class="justify-center ma-4" v-if="!hideActions">
          <slot name="actions"></slot>
        </v-card-actions>
      </v-card>
    </template>
  </v-dialog>
</template>

<style scoped>
.custom-dialog-card {
  display: flex;
  flex-direction: column;
  max-height: min(90vh, calc(100vh - 48px));
}

.custom-dialog-content {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
}

.dialog-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  width: 100%;
}

.v-toolbar,
.v-card-actions {
  flex: 0 0 auto;
}

.close-btn {
  margin-left: auto;
}
</style>
