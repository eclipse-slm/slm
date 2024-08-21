<template>
  <v-container v-if="filterValues.length > 0">
    <v-breadcrumbs class="bg-grey-lighten-5">
      <v-breadcrumbs-item class="pr-3">
        <v-icon>mdi-home</v-icon>
      </v-breadcrumbs-item>
      <div
        v-for="v in selectedFilterValues"
        :key="v"
      >
        <v-breadcrumbs-divider>/</v-breadcrumbs-divider>
        <v-breadcrumbs-item>{{ v }}</v-breadcrumbs-item>
      </div>
      <div
        v-if="showFilterSelector"
        class="mt-3"
      >
        <v-breadcrumbs-divider>/</v-breadcrumbs-divider>
        <v-breadcrumbs-item>
          <v-select
            v-model="selectedFilterValue"
            density="compact"
            :items="filterValues"
            @update:modelValue="addFilterValueToSelectedValues"
          />
        </v-breadcrumbs-item>
      </div>
      <v-spacer />
      <v-btn
        v-if="showClearSelectionButton"
        @click="clearFilterSelection" 
      >
        <v-icon icon="mdi-close" />
      </v-btn>
    </v-breadcrumbs>
  </v-container>
</template>
<script>
  export default {
    name: 'AasCircularChartFilterBar',
    props: {
      filterValues: {
        type: Array,
        default: () => [],
      },
      showFilterSelector: Boolean,
    },
    data() {
      return {
        selectedFilterValues: [],
        selectedFilterValue: "",
      }
    },
    computed: {
      showClearSelectionButton() {
        return this.selectedFilterValues.length > 0
      }
    },
    methods: {
      addFilterValueToSelectedValues(value) {
        this.selectedFilterValues.push(value)
        this.selectedFilterValue = 0
        this.$emit('selectedFilterValuesChanged', this.selectedFilterValues)
      },
      clearFilterSelection() {
        this.selectedFilterValues = []
        this.selectedFilterValue = 0
        this.$emit('selectedFilterValuesChanged', this.selectedFilterValues)
      }
    }
  }
</script>