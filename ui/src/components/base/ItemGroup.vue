<template>
  <v-list-group
    :prepend-icon="item.icon"
    :subgroup="subGroup"
    append-icon="mdi-menu-down"
    :color="store.barColor !== 'rgba(255, 255, 255, 1), rgba(255, 255, 255, 0.7)' ? 'white' : 'grey darken-1'"
  >
    <template #activator>
      <v-list-item v-if="text">
        {{ computedText }}
      </v-list-item>

      <v-list-item 
        v-else-if="item.avatar"
        class="align-self-center"
        color="white"
        contain
      >
        <v-img src="https://demos.creative-tim.com/vuetify-material-dashboard/favicon.ico" />
      </v-list-item>


      <v-list-item-title> {{ item.title }} </v-list-item-title>
    </template>

    <template v-for="(child, i) in children">
      <v-list-group
        v-if="child.children"
        :key="`sub-group-${i}`"
        :item="child"
      />

      <v-list-item
        v-else
        :key="`item-${i}`"
        :item="child"
        text
      />
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
      computedText () {
        if (!this.item || !this.item.title) return ''

        let text = ''

        this.item.title.split(' ').forEach(val => {
          text += val.substring(0, 1)
        })

        return text
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
</style>
