<template>
  <v-app>
    <v-app-bar color="primary" elevation="1">
      <v-icon color="white" class="mr-3">mdi-inbox-arrow-down</v-icon>
      <v-toolbar-title class="text-white font-weight-medium">Eclipse SLM | Installer</v-toolbar-title>
      <v-spacer />
      <v-btn
        color="white"
        variant="text"
        prepend-icon="mdi-power"
        :loading="stoppingInstaller"
        :disabled="stoppingInstaller"
        @click="showStopDialog = true"
      >
        Stop Installer
      </v-btn>
    </v-app-bar>

    <v-main>
      <v-container fluid class="installer-page py-8">
        <v-row justify="center">
          <v-col cols="12" lg="10" xl="8" class="installer-page__content">

            <PlaybookRunner />
          </v-col>
        </v-row>
      </v-container>
    </v-main>

    <ConfirmDialog
      v-model="showStopDialog"
      title="Stop Installer"
      text="Do you want to stop the installer and remove the installer containers?"
      confirm-text="Stop"
      cancel-text="Cancel"
      confirm-color="warning"
      :loading="stoppingInstaller"
      @confirm="stopInstaller"
      @cancel="showStopDialog = false"
    />
  </v-app>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import PlaybookRunner from './components/PlaybookRunner.vue';
import ConfirmDialog from './components/ConfirmDialog.vue';
import { getApiBaseUrl } from './config/runtimeConfig';

const showStopDialog = ref(false);
const stoppingInstaller = ref(false);

async function stopInstaller() {
  showStopDialog.value = false;
  stoppingInstaller.value = true;
  const apiBase = getApiBaseUrl();

  try {
    await fetch(`${apiBase}/api/installer/stop`, {
      method: 'POST'
    });
  } finally {
    // The backend call removes both installer containers, including this UI.
    setTimeout(() => {
      stoppingInstaller.value = false;
    }, 1500);
  }
}
</script>

