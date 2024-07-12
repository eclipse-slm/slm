<template>
  <v-dialog
    v-model="showDialog"
    @click:outside="onCloseButtonClicked"
  >
    <template #default="{}">
      <v-card v-if="showDialog">
        <v-toolbar
          color="primary"
          theme="dark"
        >
          {{ resource.hostname }}
        </v-toolbar>
        <v-card-text>
          <resources-info-card-single-host
            ref="resourceInfoCard"
            :resource="resource"
          />
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

<script>
  import ResourcesInfoCardSingleHost from '@/components/resources/ResourcesInfoCardSingleHost'

  export default {
    name: 'ResourcesInfoDialog',
    components: { ResourcesInfoCardSingleHost },
    props: ['resource'],
    computed: {
      showDialog () {
        console.log(this.resource)
        return this.resource !== null
      },
    },
    methods: {
      onCloseButtonClicked () {
        this.$emit('closed')
        this.$refs.resourceInfoCard.onClose()
      },
    },
  }
</script>
