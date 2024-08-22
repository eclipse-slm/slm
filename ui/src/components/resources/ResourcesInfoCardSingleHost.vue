<template>
  <v-card-text
    v-if="resource"
  >
    <v-list :opened="['Common']">
      <v-list-group
        value="Common"
      >
        <template #activator="{ props }">
          <v-list-item
            v-bind="props"
            prepend-icon="mdi-information"
            title="Common"
          />
        </template>

        <v-table>
          <tbody>
            <tr>
              <th>{{ 'Hostname' }}</th>
              <td colspan="3">
                {{ resource.hostname }}
              </td>
            </tr>
            <tr>
              <th>{{ 'IP' }}</th>
              <td colspan="3">
                {{ resource.ip }}
              </td>
            </tr>
            <tr>
              <th>{{ 'Resource ID' }}</th>
              <td colspan="3">
                {{ resource.id }}
              </td>
            </tr>
            <tr v-if="resource.remoteAccessService !== null">
              <th>{{ 'Username' }}</th>
              <td colspan="3">
                {{ resource.remoteAccessService.credential.username }}
              </td>
            </tr>
            <tr v-if="resource.remoteAccessService !== null">
              <th>{{ 'Password' }}</th>
              <td>
                <v-text-field
                  :type="showPassword ? 'text' : 'password'"
                  :model-value="resource.remoteAccessService.credential.password"
                  disabled
                  hide-details="auto"
                />
              </td>
              <td class="btn-col">
                <v-btn
                  color="info"
                  @click="togglePasswordShow"
                >
                  <v-icon
                      :icon="showPassword ? 'mdi-glasses' : 'mdi-sunglasses'"
                      color="white"
                  />
                </v-btn>
              </td>
              <td class="btn-col">
                <v-btn
                  color="info"
                  @click="copyOtp"
                >
                  <v-icon icon="mdi-content-copy" />
                </v-btn>
              </td>
              <td
                v-if="resource.passwordType === 'otp'"
                class="btn-col"
              >
                <v-btn
                  color="info"
                  @click="updateOtp"
                >
                  <v-icon icon="mdi-reload" />
                </v-btn>
              </td>
            </tr>
          </tbody>
        </v-table>
      </v-list-group>
      <input
        id="otp"
        type="hidden"
        :value="resource.password"
      >
      <v-list-group value="Hardware">
        <template #activator="{props}">
          <v-list-item
            v-bind="props"
            prepend-icon="mdi-expansion-card"
            title="Hardware"
          />
        </template>
        <resource-metrics
          ref="resourceMetrics"
          :resource-id="resource.id"
        />
      </v-list-group>
      <v-list-group value="Submodels">
        <template #activator="{props}">
          <v-list-item
            v-bind="props"
            prepend-icon="mdi-adjust"
            title="Submodels"
          />
        </template>
        <resource-submodels
          ref="resourceSubmodels"
          :resource-id="resource.id"
        />
      </v-list-group>
    </v-list>
  </v-card-text>
</template>

<script>
import ResourceMetrics from '@/components/resources/ResourceMetrics'
import ResourceSubmodels from '@/components/resources/ResourceSubmodels'
import {useResourcesStore} from "@/stores/resourcesStore";

export default {
    name: 'ResourcesInfoCardSingleHost',
    components: { ResourceMetrics, ResourceSubmodels },
    props: {
      resource: {
        type: Object,
        default: null,
      },
    },
    setup(props){
      console.log(props.resource);
    },
    data () {
      return {
        otp: '',
        showPassword: false,
        pollResourceMetrics: true,
      }
    },
    methods: {
      onClose () {
        if(this.$refs.resourcesMetrics)
          this.$refs.resourceMetrics.onClose()
      },
      togglePasswordShow () {
        this.showPassword = !this.showPassword
      },
      updateOtp () {
        const resourceStore = useResourcesStore();
        resourceStore.updateOtp();
      },
      copyOtp () {
        const otpInput = document.querySelector('#otp')
        otpInput.setAttribute('type', 'text')
        otpInput.select()

        try {
          document.execCommand('copy')
        } catch (err) {
          console.error('Failed to copy otp')
        }

        otpInput.setAttribute('type', 'hidden')
        window.getSelection().removeAllRanges()
      },
    },
  }
</script>

<style scoped>
.btn-col {
  width:1px;
  white-space: nowrap;
}
</style>
