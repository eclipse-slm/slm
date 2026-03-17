<template>
  <span 
    v-if="options && options.length > 0" 
    class="nav-item" 
    data-app
  >
      <v-row>
        <v-col cols="3" align="right">Version:</v-col>
        <v-col cols="5" align="left">
          <v-select
            v-model="selected" 
            :items="options"
            outlined
            dense
            @change="onChange"
          />
        </v-col>
      </v-row>
  </span>
</template>

<script>
import Axios from 'axios';
export default {
  data() {
    return {
      selected: undefined,
      options: [],
    };
  },
  created: async function() {
    try {
      let response = await Axios.get('https://api.github.com/repos/eclipse-slm/slm/git/trees/github-pages');
      const versionNode = response.data.tree.find(e => {
        return e.path.toLowerCase() === 'version';
      });
      response = await Axios.get(versionNode.url);
      this.options = response.data.tree.map(e => {
        return {value: e.path, text: e.path};
      });
      this.options.sort((e1, e2) => {
        const e1Arr = e1.text.split('.');
        const e2Arr = e2.text.split('.');
        for (let i = 0; i < e1Arr.length && i < e2Arr.length; i++) {
          const e1V = parseInt(e1Arr[i]);
          const e2V = parseInt(e2Arr[i]);
          if (e1V !== e2V) return e2V - e1V;
          if (e1Arr[i] !== e2Arr[i]) return e2Arr[i] - e1Arr[i];
        }
        return e1.text === e2.text ? 0 : e2.text < e1.text ? -1 : 1;
      });
      this.options.unshift({value: 'latest', text: 'latest'});

      const path = window.location.pathname.toLowerCase();
      let regex = new RegExp('/version/([0-9]+.[0-9]+)');
      let isVersionInPath = regex.test(path);
      if (isVersionInPath) {
          let version = regex.exec(path)[1]
          this.selected = version;
      } else {
        this.selected = 'latest';
      }
    } catch (ex) {}
  },
  methods: {
    onChange(event) {
      let targetVersionPath = ''
      if (this.selected === 'latest') {
        targetVersionPath = ''
      } else {
        targetVersionPath = `/version/${this.selected}`
      }
      
      let path = window.location.pathname.toLowerCase();
      let regex = new RegExp('/version/[0-9]+.[0-9]+');
      let isVersionInPath = regex.test(path);
      if (isVersionInPath) {
          let versionPathSegment = regex.exec(path)[0]
          path = path.replace(versionPathSegment, targetVersionPath)
      }
      else {
          let pathRest = path.split("/slm/")[1]
          path = "/slm" + targetVersionPath + "/" + pathRest
      }

      console.log(path)
      window.location.pathname = path
    },
  },
};
</script>
