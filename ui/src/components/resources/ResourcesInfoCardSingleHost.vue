<template>
  <v-card-text
    v-if="resource"
  >
    <v-list>
      <v-list-group
        :model-value="true"
      >
        <template #activator>
          <v-list-item>
            <v-icon>mdi-information</v-icon>
          </v-list-item>
          <v-list-item>
            <v-list-item-title>
              Common
            </v-list-item-title>
          </v-list-item>
        </template>

        <v-table v-slot>
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
                  <v-icon v-if="showPassword">
                    mdi-glasses
                  </v-icon>
                  <v-icon v-else>
                    mdi-sunglasses
                  </v-icon>
                </v-btn>
              </td>
              <td class="btn-col">
                <v-btn
                  color="info"
                  @click="copyOtp"
                >
                  <v-icon>
                    mdi-content-copy
                  </v-icon>
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
                  <v-icon>mdi-reload</v-icon>
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
      <v-list-group>
        <template #activator>
          <v-list-item>
            <v-icon>mdi-expansion-card</v-icon>
          </v-list-item>
          <v-list-item>
            <v-list-item-title>
              Hardware
            </v-list-item-title>
          </v-list-item>
        </template>
        <resource-metrics
          ref="resourceMetrics"
          :resource-id="resource.id"
        />
      </v-list-group>
      <v-list-group>
        <template #activator>
          <v-list-item>
            <v-icon>mdi-adjust</v-icon>
          </v-list-item>
          <v-list-item>
            <v-list-item-title>
              Submodels
            </v-list-item-title>
          </v-list-item>
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

  export default {
    name: 'ResourcesInfoCardSingleHost',
    components: { ResourceMetrics, ResourceSubmodels },
    props: {
      resource: {
        type: Object,
        default: null,
      },
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
        this.$store.dispatch('updateOtp')
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
