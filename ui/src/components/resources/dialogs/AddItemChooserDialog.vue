<script setup>
import CustomDialog from '@/components/base/CustomDialog.vue';

defineProps({
  show: {
    type: Boolean,
    default: false
  }
});
const emit = defineEmits(['device', 'capability', 'canceled']);
</script>

<template>
  <CustomDialog
    :show="show"
    title="Add"
    width="500"
    :hide-actions="true"
    @canceled="emit('canceled')"
  >
    <template #content>
      <div class="d-flex ga-4">
        <v-card
          class="chooser-tile flex-1-1 text-center pa-4"
          variant="outlined"
          hover
          @click="emit('device')"
        >
          <v-icon
            icon="mdi-devices"
            size="48"
            color="primary"
          />
          <div class="text-h6 mt-2">
            Device
          </div>
          <div class="text-caption">
            Register a new device
          </div>
        </v-card>
        <v-card
          class="chooser-tile flex-1-1 text-center pa-4"
          variant="outlined"
          hover
          @click="emit('capability')"
        >
          <v-icon
            icon="mdi-puzzle"
            size="48"
            color="primary"
          />
          <div class="text-h6 mt-2">
            Capability
          </div>
          <div class="text-caption">
            Define a new capability
          </div>
        </v-card>
      </div>
    </template>
  </CustomDialog>
</template>

<style scoped>
.chooser-tile {
  cursor: pointer;
}
</style>
