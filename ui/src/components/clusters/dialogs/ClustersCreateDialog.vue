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
      <clusters-create-dialog-page-start
        v-if="page === ClustersCreateDialogPage.START"
        @page-changed="onPageChanged"
        @title-changed="onTitleChanged"
        @canceled="closeDialog"
        @confirmed="closeDialog"
      />

      <clusters-create-dialog-page-add-existing-cluster
        v-if="page === ClustersCreateDialogPage.ADD_EXISTING_CLUSTER"
        @page-changed="onPageChanged"
        @title-changed="onTitleChanged"
        @canceled="closeDialog"
        @confirmed="closeDialog"
      />

      <clusters-create-dialog-page-create-new-cluster
        v-if="page === ClustersCreateDialogPage.CREATE_CLUSTER"
        @page-changed="onPageChanged"
        @title-changed="onTitleChanged"
        @confirmed="closeDialog"
        @canceled="closeDialog"
      />
    </template>
  </v-dialog>
</template>
<script>
  import ClustersCreateDialogPage from "@/components/clusters/dialogs/ClustersCreateDialogPage";
  import ClustersCreateDialogPageStart from "@/components/clusters/dialogs/ClustersCreateDialogPageStart.vue";
  import ClustersCreateDialogPageCreateNewCluster
    from "@/components/clusters/dialogs/ClustersCreateDialogPageCreateNewCluster.vue";
  import ClustersCreateDialogPageAddExistingCluster
    from "@/components/clusters/dialogs/ClustersCreateDialogPageAddExistingCluster.vue";

  export default {
    name: 'ClusterCreateDialog',
    components: {
      ClustersCreateDialogPageStart,
      ClustersCreateDialogPageCreateNewCluster,
      ClustersCreateDialogPageAddExistingCluster
    },
    props: ['show'],
    data () {
      return {
        page: ClustersCreateDialogPage.START,
        title: ''
      }
    },
    computed: {
      ClustersCreateDialogPage() {
        return ClustersCreateDialogPage
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
        this.page = ClustersCreateDialogPage.START
        this.title= ''
      },
    }
  }
</script>