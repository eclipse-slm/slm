<template>
  <v-container>
    <!-- Service Description !-->
    <v-row>
      <base-material-card
        flat
        width="100%"
        color="secondary"
      >
        <template #heading>
          <div>
            {{ $t('labels.Description') }}
          </div>
        </template>
        <v-card-text>
          {{ serviceOffering.description }}
        </v-card-text>
      </base-material-card>
    </v-row>
    <!-- Service Images !-->
    <v-row>
      <base-material-card
        v-if="serviceOffering.images"
        flat
        width="100%"
        color="secondary"
      >
        <template #heading>
          <div>
            {{ $t('ServiceLabels.Images') }}
          </div>
        </template>
        <v-row class="pa-4">
          <v-col cols="4">
            <v-img
              :src="serviceOffering.img"
              height="200px"
            />
          </v-col>
          <v-col cols="4">
            <v-img
              :src="serviceOffering.img"
              height="200px"
            />
          </v-col>
          <v-col cols="4">
            <v-img
              :src="serviceOffering.img"
              height="200px"
            />
          </v-col>
        </v-row>
      </base-material-card>
    </v-row>
    <!-- Service Documents !-->
    <v-row>
      <base-material-card
        v-if="serviceOffering.documents"
        flat
        width="100%"
        color="secondary"
      >
        <template #heading>
          <div>
            {{ $t('ServiceLabels.Documents') }}
          </div>
        </template>
        <v-card-text>
          Doc 1
        </v-card-text>
        <v-card-text>
          Doc 2
        </v-card-text>
      </base-material-card>
    </v-row>
  </v-container>
</template>

<script>
  export default {
    name: 'ServiceDetailsOverview',
    props: {
      serviceOffering: { type: Object },
    },
  }
</script>

<style scoped>
</style>
