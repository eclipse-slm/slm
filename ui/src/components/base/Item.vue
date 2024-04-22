<template>
  <v-list-item
    :href="href"
    :rel="href && href !== '#' ? 'noopener' : undefined"
    :target="href && href !== '#' ? '_blank' : undefined"
    :to="item.to"
    :active-class="`primary ${!isDark ? 'black' : 'white'}--text`"
  >
    <template #prepend>
      <v-icon v-if="text" class="v-list-item__icon--text">
        {{ computedText }}
      </v-icon>
      <v-icon v-else-if="item.icon">
        {{ item.icon }}
      </v-icon>
    </template>

    <v-list-item-title v-if="item.title">
      {{ item.title }}
    </v-list-item-title>
    <v-list-item-subtitle v-if="item.subtitle">
      {{ item.subtitle }}
    </v-list-item-subtitle>
  </v-list-item>
</template>

<script>
  import Themeable from 'vuetify'

  export default {
    // eslint-disable-next-line vue/multi-word-component-names
    name: 'Item',

    mixins: [Themeable],

    props: {
      item: {
        type: Object,
        default: () => ({
          href: undefined,
          icon: undefined,
          subtitle: undefined,
          title: undefined,
          to: undefined,
        }),
      },
      text: {
        type: Boolean,
        default: false,
      },
    },

    computed: {
      computedText () {
        if (!this.item || !this.item.title) return ''

        let text = ''

        this.item.title.split(' ').forEach(val => {
          text += val.substring(0, 1)
        })

        return text
      },
      href () {
        return this.item.href || (!this.item.to ? '#' : undefined)
      },
    },
  }
</script>
