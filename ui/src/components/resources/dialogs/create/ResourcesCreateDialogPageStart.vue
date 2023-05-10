<template>
  <v-card>
    <v-container>
      <v-row class="my-3">
        <v-col class="text-center">
          <v-btn
            id="resource-create-button-add-existing"
            color="secondary"
            x-large
            tile
            @click="$emit('page-changed', ResourcesCreateDialogPage.ADD_EXISTING_RESOURCE)"
          >
            <v-icon
              class="mr-5"
              x-large
              left
            >
              mdi-plus
            </v-icon>
            Add existing
          </v-btn>
        </v-col>
        <v-col class="text-center">
          <v-btn
            id="resource-create-button-create-new"
            color="secondary"
            x-large
            tile
            @click="$emit('page-changed', ResourcesCreateDialogPage.CREATE_RESOURCE)"
          >
            <v-icon
              class="mr-5"
              large
              left
            >
              mdi-pencil-outline
            </v-icon>
            Create new resource
          </v-btn>
        </v-col>
      </v-row>
    </v-container>
    <v-card-actions class="justify-end">
      <v-btn
        text
        @click="$emit('canceled')"
      >
        Cancel
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
import ResourcesCreateDialogPage from "@/components/resources/dialogs/create/ResourcesCreateDialogPage";

export default {
    name: 'ResourcesCreateDialogPageStart',
    components: { },
    props: ['show'],
    enums: {
      ResourcesCreateDialogPage,
    },
    mounted() {
        this.$emit('title-changed', '')
    },
    methods: {
      },
    }
</script>
