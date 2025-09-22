<script setup lang="ts">
import {onMounted, ref} from "vue";
import ProgressCircular from "@/components/base/ProgressCircular.vue";
import RowWithLabel from "@/components/base/RowWithLabel.vue";

const props = defineProps({
  resourceType: {
    type: Object,
    default: undefined
  }
});

const loading = ref(false);
const loadData = () => {
  // // Get aas descriptor
  // ResourceManagementClient.resourcesApi.getResource(props.resourceId).then(response => {
  //   resource.value = response.data
  // }).catch((e) => {
  //   resource.value = undefined;
  //
  // }).finally(() => {
  //   loading.value = false
  // })
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <v-container fluid>
    <div v-if="loading">
      <progress-circular />
    </div>

    <div v-else>
      <RowWithLabel
        label="Product"
        :text="props.resourceType.typeName"
      />
      <RowWithLabel
        label="Vendor"
        :text="props.resourceType.manufacturerName"
      />
      <RowWithLabel
        label="Device Instances"
        :text="props.resourceType.resourceInstanceIds.length"
      />
    </div>
  </v-container>
</template>

<style scoped>
.v-divider {
  border-color: #000000;
}
</style>