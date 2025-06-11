<template>
  <v-card>
    <v-container>
      <v-row class="my-3">
        <v-col class="text-center">
          <v-btn
            id="resource-create-button-add-existing"
            color="secondary"
            size="x-large"
            tile
            @click="$emit('page-changed', ResourcesCreateDialogPage.ADD_EXISTING_RESOURCE)"
          >
            <v-icon
              class="mr-5"
              size="x-large"
              start
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
            size="x-large"
            tile
            @click="$emit('page-changed', ResourcesCreateDialogPage.CREATE_RESOURCE)"
          >
            <v-icon
              class="mr-5"
              size="large"
              start
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
        variant="text"
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
    props: {
      show: {
        type: Boolean,
        default: false
      }
    },
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
