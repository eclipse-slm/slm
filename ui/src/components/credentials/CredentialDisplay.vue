<script setup lang="ts">
import RowWithLabel from "@/components/base/RowWithLabel.vue";
import { computed } from 'vue'
import {CredentialDataType} from "@/api/platform-management/client";
import {useToast} from "vue-toast-notification";
import {useClipboard} from "@vueuse/core";

const props = defineProps({
  credential: { type: Object, required: true },
  hideName: { type: Boolean, default: false },
})

const $toast = useToast();
const { copy, isSupported } = useClipboard()

const credentialDataType = computed(() => props.credential?.data?.credentialDataType)

const copyToClipboard = (content) => {
  copy(content)
  $toast.info(`Copied to clipboard`)
}
</script>

<template>
  <div>
    <!-- CREDENTIAL NAME -->
    <RowWithLabel
      v-if="!props.hideName"
      label="Name"
      :text="props.credential?.name ?? '-'"
    />
    <!-- USERNAME / PASSWORD -->
    <div v-if="credentialDataType === CredentialDataType.UsernamePassword">
      <RowWithLabel
          label="Username"
          :text="props.credential?.data?.username"
          :copyable="true"
      />

      <RowWithLabel label="Password" :divider="false">
        <template #content>
          <v-tooltip>
            <template #activator="{ props: tp }">
              <span v-bind="tp">••••••••</span>
            </template>
            <span>Password is secret and cannot be read.</span>
          </v-tooltip>
        </template>
      </RowWithLabel>
    </div>

    <!-- KEY PAIR -->
    <div v-else-if="credentialDataType === CredentialDataType.KeyPair">
      <RowWithLabel label="Public Key" >
        <template #content>
          <v-row>
            <v-col cols="11">
              <v-textarea
                readonly
                :value="credential?.data?.publicKey ?? ''"
                auto-grow
                rows="1"
                max-rows="6"
                outlined
                class="public-key-textarea"
              />
            </v-col>
            <v-col cols="1" v-if="isSupported">
              <v-btn
                color="secondary"
                size="small"
                @click="copyToClipboard(props.credential?.data?.publicKey)"
              >
                <v-icon icon="mdi-content-copy" color="white" />
              </v-btn>
            </v-col>
          </v-row>
        </template>
      </RowWithLabel>

      <RowWithLabel label="Private Key">
        <template #content>
          <v-tooltip>
            <template #activator="{ props: tp }">
              <span v-bind="tp">••••••••</span>
            </template>
            <span>Private key is secret and cannot be read.</span>
          </v-tooltip>
        </template>
      </RowWithLabel>
    </div>

    <div v-else>
      Unsupported credential type '{{ credentialDataType }}', cannot display.
    </div>

  </div>
</template>

<style scoped>
.public-key-textarea textarea {
  max-height: 160px;
  overflow-y: auto;
}
</style>
