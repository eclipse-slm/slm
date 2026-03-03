<script setup lang="ts">
import RowWithLabel from "@/components/base/RowWithLabel.vue";
import DeviceUtils from "@/utils/deviceUtils";
import { useResourceDevicesStore } from "@/stores/resourceDevicesStore";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import {ResourceUpdateRequest} from "@/api/resource-management/client";

const props = defineProps({
  resourceId: {
    type: String,
    required: true,
  },
});

const resourceDevicesStore = useResourceDevicesStore();

const onProductUpdated = (newProduct: string) => {
  const resourceUpdateRequest = {
    product: newProduct
  } as ResourceUpdateRequest;
  ResourceManagementClient.resourcesApi.updateResource(props.resourceId, resourceUpdateRequest);
};

const onManufacturerUpdated = (newManufacturer: string) => {
  const resourceUpdateRequest = {
    vendor: newManufacturer
  } as ResourceUpdateRequest;
  ResourceManagementClient.resourcesApi.updateResource(props.resourceId, resourceUpdateRequest);
};

const onAssetIdUpdated = (newAssetId: string) => {
  const resourceUpdateRequest = {
    assetId: newAssetId
  } as ResourceUpdateRequest;
  ResourceManagementClient.resourcesApi.updateResource(props.resourceId, resourceUpdateRequest);
};

const onHostnameUpdated = (newHostname: string) => {
  const resourceUpdateRequest = {
    hostname: newHostname
  } as ResourceUpdateRequest;
  ResourceManagementClient.resourcesApi.updateResource(props.resourceId, resourceUpdateRequest);
};

const onIpUpdated = (newIp: string) => {
  const resourceUpdateRequest = {
    ip: newIp
  } as ResourceUpdateRequest;
  ResourceManagementClient.resourcesApi.updateResource(props.resourceId, resourceUpdateRequest);
}
</script>

<template>
  <div>
    <RowWithLabel
      label="Product"
      :text="DeviceUtils.getProduct(resourceId)"
      :editable="true"
      :copyable="true"
      @value-updated="onProductUpdated"
    />
    <RowWithLabel
      label="Vendor"
      :text="DeviceUtils.getManufacturer(resourceId)"
      :editable="true"
      :copyable="true"
      @value-updated="onManufacturerUpdated"
    />
    <RowWithLabel
      label="Resource Id"
      :text="resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId, 'DeviceInfo', '$.Id')"
      :editable="false"
      :copyable="true"
    />
    <RowWithLabel
      label="Asset Id"
      :text="resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId, 'DeviceInfo', '$.AssetId')"
      :editable="true"
      :copyable="true"
      @value-updated="onAssetIdUpdated"
    />
    <RowWithLabel
      label="Hostname"
      :text="resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId, 'DeviceInfo', '$.Hostname')"
      :editable="true"
      :copyable="true"
      validation-name="Hostname"
      :validation-rules="(v: string) => /^[A-Za-z0-9_-]+$/.test(v) || 'Invalid hostname'"
      @value-updated="onHostnameUpdated"
    />
    <RowWithLabel
      label="IP"
      :text="resourceDevicesStore.getSubmodelElementValueOfResourceSubmodel(resourceId, 'DeviceInfo', '$.IP')"
      :editable="true"
      :copyable="true"
      validation-name="IP"
      :validation-rules="(v: string) => /^\d{1,3}(?:\.\d{1,3}){3}$/.test(v) || 'Invalid IP'"
      @value-updated="onIpUpdated"
    />
  </div>
</template>
