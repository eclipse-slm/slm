<template>
  <v-app>
    <v-app-bar color="primary" elevation="1">
      <v-icon color="white" class="mr-3">mdi-inbox-arrow-down</v-icon>
      <v-toolbar-title class="text-white font-weight-medium">Eclipse SLM | Installer</v-toolbar-title>
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
  </v-app>
</template>

<script setup lang="ts">
import PlaybookRunner from './components/PlaybookRunner.vue';
</script>

