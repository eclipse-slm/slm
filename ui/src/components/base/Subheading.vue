<template>
  <div class="text-h3 font-weight-light col col-12 text-left text--primary pa-0 mb-8">
    <h5 class="font-weight-light">
      {{ subheading }}
      <template v-if="text">
        <span
          class="text-subtitle-1"
          v-text="text"
        />
      </template>
    </h5>

    <div class="pt-2">
      <slot />
    </div>
  </div>
</template>

<script>
  export default {
    // eslint-disable-next-line vue/multi-word-component-names
    name: 'Subheading',

    props: {
      subheading: {
        type: String,
        default: '',
      },
      text: {
        type: String,
        default: '',
      },
    },
  }
</script>
