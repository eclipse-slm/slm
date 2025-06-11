<template>
  <v-list-group
    :value="item.title"
    :prepend-icon="item.icon"
    :subgroup="subGroup"
    append-icon="mdi-menu-down"
    :color="store.barColor !== 'rgba(255, 255, 255, 1), rgba(255, 255, 255, 0.7)' ? 'white' : 'grey darken-1'"
  >
    <template #activator="{ props }">
      <v-list-item
        v-if="text"
        v-bind="props"
      >
        <v-list-item-title>
          {{ item.title }}
        </v-list-item-title>
      </v-list-item>

      <v-list-item
        v-else-if="item.icon"
        v-bind="props"
        :tile="item.title"
        :prepend-icon="item.icon"
        class="align-self-center"
        color="white"
        contain
      />
    </template>

    <template v-for="(child, i) in children">
      <div
        v-if="child.visible"
        :key="`sub-group-${i}`"
      >
        <v-list-group
          v-if="child.children"
          :item="child"
        />

        <base-item
          v-else
          :id="child.id"
          :key="`item-${i}`"
          :item="child"
        />
      </div>
    </template>
  </v-list-group>
</template>

<script>
// Utilities
import kebabCase from 'lodash/kebabCase'

import {useStore} from "@/stores/store";

export default {
    name: 'ItemGroup',

    inheritAttrs: false,

    props: {
      item: {
        type: Object,
        default: () => ({
          avatar: undefined,
          group: undefined,
          title: undefined,
          children: [],
        }),
      },
      subGroup: {
        type: Boolean,
        default: false,
      },
      text: {
        type: Boolean,
        default: false,
      },
    },
    setup(){
      const store = useStore();
      return {store};
    },
    computed: {
      children () {
        return this.item.children.map(item => ({
          ...item,
          to: !item.to ? undefined : `${this.item.group}/${item.to}`,
        }))
      },
      group () {
        return this.genGroup(this.item.children)
      },
    },

    methods: {
      genGroup (children) {
        return children
          .filter(item => item.to)
          .map(item => {
            const parent = item.group || this.item.group
            let group = `${parent}/${kebabCase(item.to)}`

            if (item.children) {
              group = `${group}|${this.genGroup(item.children)}`
            }

            return group
          }).join('|')
      },
    },
  }
</script>

<style>
.v-list-group__activator p {
  margin-bottom: 0;
}

.v-list-group {
  --list-indent-size: 16px;
  --prepend-width: 0px;
}

</style>
