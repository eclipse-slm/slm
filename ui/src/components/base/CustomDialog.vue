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
</script>

<template>
  <v-dialog
      v-model="dialogActive"
      :width="width"
      @click:outside="emit('canceled')"
  >
    <template #default="{}">
      <v-card v-if="dialogActive">
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
        <v-card-text class="mt-8 mx-4">
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
.dialog-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  width: 100%;
}
.close-btn {
  margin-left: auto;
}
</style>
