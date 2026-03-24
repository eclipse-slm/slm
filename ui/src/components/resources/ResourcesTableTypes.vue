<script setup lang="ts">
import {ref} from 'vue';
import {useResourceDevicesStore} from "@/stores/resourceDevicesStore";
import FirmwareUpdateVersion from "@/components/updates/FirmwareUpdateVersion.vue";

const props = defineProps({
  resourceTypes: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['resource-type-selected', 'resource-instance-selected']);

const resourceDevicesStore = useResourceDevicesStore();
const expanded = ref([]);

const tableHeadersResourceTypes = [
  { title: 'Manufacturer', key: 'manufacturerName', width: "35%" },
  { title: 'Type Name', key: 'typeName', width: "55%"  },
  { title: 'Instances', key: 'resourceInstanceCount', width: "10%"  }
];

const tableHeadersResourceInstancesOfType = [
  { title: 'Hostname', key: 'hostname', width: "25%" },
  { title: 'IP', key: 'ip', width: "25%"  },
  { title: 'ID', key: 'id', width: "30%"  },
  { title: 'Firmware', key: 'firmware', width: "20%"  },
];

const onRowClickResourceTypes = (click, row) => {
  emit('resource-type-selected', row.item);
};

const onRowClickResourceInstance = (click, row) => {
  console.log(row.item.id)
  emit('resource-instance-selected', row.item, "common");
};
</script>

<template>
  <div>
    <v-data-table
      :expanded="expanded"
      :headers="tableHeadersResourceTypes"
      :items="props.resourceTypes"
      item-key="typeName"
      item-value="typeName"
      class="elevation-1"
      show-expand
      :items-per-page="25"
      @click:row="onRowClickResourceTypes"
    >
      <template #item.resourceInstanceCount="{ item }">
        {{ item.resourceInstanceIds ? item.resourceInstanceIds.length : 0 }}
      </template>


      <template #expanded-row="{ item }">
        <td
          :colspan="tableHeadersResourceTypes.length+1"
          class="expanded-row-spacing expanded-row-highlight"
        >
          <v-data-table
            :headers="tableHeadersResourceInstancesOfType"
            :items="item.resourceInstanceIds.map(id => ({ id }))"
            hide-default-footer
            :items-per-page="-1"
            class="expanded-table"
            @click:row="onRowClickResourceInstance"
          >
            <template #item.hostname="{ item }">
              {{ resourceDevicesStore.resourceById(item.id)?.hostname ?? "N/A" }}
            </template>
            <template #item.ip="{ item }">
              {{ resourceDevicesStore.resourceById(item.id)?.ip ?? "N/A" }}
            </template>
            <template #item.id="{ item }">
              {{ item.id }}
            </template>
            <template #item.firmware="{ item }">
              <FirmwareUpdateVersion
                :resource-id="item.id"
                @click="emit('resource-instance-selected', item, 'firmware')"
              />
            </template>
          </v-data-table>
        </td>
      </template>
    </v-data-table>
  </div>
</template>

<style scoped>
.expanded-row-spacing {
  border-bottom: 15px solid transparent;
}

.expanded-row-highlight {
  background:  rgba(var(--v-theme-secondary), 0.04);
}

.expanded-table {
  background: rgba(0, 0, 0, 0.00) !important;
}
</style>