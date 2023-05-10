<template>
  <v-dialog
    v-model="show"
    max-width="800px"
    @click:outside="closeDialog"
  >
    <template>
      <v-toolbar
        color="primary"
        dark
      >
        <v-row
          align="center"
          justify="center"
        >
          <v-col cols="11">
            <div class="font-weight-light">
              {{ title }}
            </div>
          </v-col>
          <v-spacer />
          <v-col
            cols="1"
          >
            <v-btn
              icon
              @click="closeDialog"
            >
              <v-icon>mdi-close</v-icon>
            </v-btn>
          </v-col>
        </v-row>
      </v-toolbar>
      <resources-create-dialog-page-start
        v-if="page === ResourcesCreateDialogPage.START"
        @page-changed="onPageChanged"
        @title-changed="onTitleChanged"
        @canceled="closeDialog"
        @confirmed="closeDialog"
      />

      <resources-create-dialog-page-add-existing-resource
        v-if="page === ResourcesCreateDialogPage.ADD_EXISTING_RESOURCE"
        @page-changed="onPageChanged"
        @title-changed="onTitleChanged"
        @canceled="closeDialog"
        @confirmed="closeDialog"
      />

      <resources-create-dialog-page-add-existing-resource-host
        v-if="page === ResourcesCreateDialogPage.ADD_EXISTING_RESOURCE_HOST"
        @page-changed="onPageChanged"
        @title-changed="onTitleChanged"
        @canceled="closeDialog"
        @confirmed="closeDialog"
      />

      <resources-create-dialog-page-add-existing-resource-cluster
        v-if="page === ResourcesCreateDialogPage.ADD_EXISTING_RESOURCE_CLUSTER"
        @page-changed="onPageChanged"
        @title-changed="onTitleChanged"
        @canceled="closeDialog"
        @confirmed="closeDialog"
      />

      <resources-create-dialog-page-create-new-resource
        v-if="page === ResourcesCreateDialogPage.CREATE_RESOURCE"
        @page-changed="onPageChanged"
        @title-changed="onTitleChanged"
        @canceled="closeDialog"
        @confirmed="closeDialog"
      />

      <resources-create-dialog-page-create-new-resource-cluster
        v-if="page === ResourcesCreateDialogPage.CREATE_RESOURCE_CLUSTER"
        @page-changed="onPageChanged"
        @title-changed="onTitleChanged"
        @confirmed="closeDialog"
        @canceled="closeDialog"
      />
    </template>
  </v-dialog>
</template>

<script>
  import ResourcesCreateDialogPage from "@/components/resources/dialogs/create/ResourcesCreateDialogPage";
  import ResourcesCreateDialogPageStart from "@/components/resources/dialogs/create/ResourcesCreateDialogPageStart";
  import ResourcesCreateDialogPageAddExistingResourceHost
    from "@/components/resources/dialogs/create/ResourcesCreateDialogPageAddExistingResourceHost";
  import ResourcesCreateDialogPageAddExistingResource
    from "@/components/resources/dialogs/create/ResourcesCreateDialogPageAddExistingResource";
  import ResourcesCreateDialogPageAddExistingResourceCluster
    from "@/components/resources/dialogs/create/ResourcesCreateDialogPageAddExistingResourceCluster";
  import ResourcesCreateDialogPageCreateNewResource
    from "@/components/resources/dialogs/create/ResourcesCreateDialogPageCreateNewResource";
  import ResourcesCreateDialogPageCreateNewResourceCluster
    from "@/components/resources/dialogs/create/ResourcesCreateDialogPageCreateNewResourceCluster";

  export default {
    name: 'ResourcesCreateDialog',
    components: {
      ResourcesCreateDialogPageStart,
      ResourcesCreateDialogPageAddExistingResource,
      ResourcesCreateDialogPageAddExistingResourceHost,
      ResourcesCreateDialogPageAddExistingResourceCluster,
      ResourcesCreateDialogPageCreateNewResource,
      ResourcesCreateDialogPageCreateNewResourceCluster
    },
    props: ['show'],
    enums: {
      ResourcesCreateDialogPage,
    },
    data () {
      return {
        page: ResourcesCreateDialogPage.START,
        title: ''
      }
    },
    methods: {
      onPageChanged(newPage) {
        this.page = newPage;
      },
      onTitleChanged(newTitle) {
        this.title = newTitle;
      },
      closeDialog () {
        this.$emit('canceled')
        this.page = ResourcesCreateDialogPage.START
        this.title= ''
      },
    },
  }
</script>
