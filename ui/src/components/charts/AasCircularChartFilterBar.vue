<template>
  <v-container v-if="this.filterValues.length > 0">
    <v-breadcrumbs class="grey lighten-5" >
      <v-breadcrumbs-item class="pr-3"><v-icon>mdi-home</v-icon></v-breadcrumbs-item>
      <div v-for="v in selectedFilterValues" v-bind:key="v">
        <v-breadcrumbs-divider>/</v-breadcrumbs-divider>
        <v-breadcrumbs-item>{{ v }}</v-breadcrumbs-item>
      </div>
      <div v-if="this.showFilterSelector" class="mt-3">
        <v-breadcrumbs-divider>/</v-breadcrumbs-divider>
        <v-breadcrumbs-item>
          <v-select dense :items="filterValues" @change="addFilterValueToSelectedValues" v-model="selectedFilterValue" />
        </v-breadcrumbs-item>
      </div>
      <v-spacer></v-spacer>
      <v-btn icon @click="clearFilterSelection" v-if="showClearSelectionButton"><v-icon>mdi-close</v-icon></v-btn>
    </v-breadcrumbs>
  </v-container>
</template>
<script>
  export default {
    name: 'AasCircularChartFilterBar',
    props: {
      filterValues: Array,
      showFilterSelector: Boolean
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