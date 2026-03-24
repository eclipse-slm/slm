<template>
  <VPNavBarMenuGroup @change="onChange" @click="updateSelected" :item=item v-if="options.length > 0">
  </VPNavBarMenuGroup>
</template>


<script setup>
import VPNavBarMenuGroup from 'vitepress/dist/client/theme-default/components/VPNavBarMenuGroup.vue';
import { onMounted, ref } from 'vue';

const basePath = "/slm"
const versionPathSegment = 'version'
const selected = ref(extractVersionFromPath(window.location.pathname));
const options = ref([]);
const item = ref({
  text: selected,
  items: options
})

function compareVersionStrings(left, right) {
  const leftParts = left.split('.');
  const rightParts = right.split('.');
  const maxLength = Math.max(leftParts.length, rightParts.length);

  for (let index = 0; index < maxLength; index += 1) {
    const leftPart = leftParts[index] ?? '0';
    const rightPart = rightParts[index] ?? '0';
    const leftNumber = Number.parseInt(leftPart, 10);
    const rightNumber = Number.parseInt(rightPart, 10);

    if (!Number.isNaN(leftNumber) && !Number.isNaN(rightNumber) && leftNumber !== rightNumber) {
      return rightNumber - leftNumber;
    }

    if (leftPart !== rightPart) {
      return rightPart.localeCompare(leftPart);
    }
  }

  return 0;
}

function extractVersionFromPath(pathname) {
  const match = pathname.match(new RegExp(`\/${versionPathSegment}\/([^/]+)`));
  return match?.[1] ?? 'latest';
}

function buildPathForVersion(pathname, version) {
  const pathMatch = pathname.match(new RegExp(`^\/slm(?:\/${versionPathSegment}\/[^/]+)?(\/.*)?$`, 'i'));
  if (!pathMatch) {
    return pathname;
  }

  const restPath = pathMatch[1] ?? '/';
  const versionPrefix = version === 'latest' ? '' : `/${versionPathSegment}/${version}`;
  return `/slm${versionPrefix}${restPath}`;
}

async function loadVersions() {
  const rootResponse = await fetch('https://api.github.com/repos/eclipse-slm/slm/git/trees/github-pages');
  const rootData = await rootResponse.json();
  const versionNode = rootData?.tree?.find((entry) => entry.path?.toLowerCase() === 'version');
  if (!versionNode?.url) {
    return;
  }

  const versionResponse = await fetch(versionNode.url);
  const versionData = await versionResponse.json();
  const mappedOptions = (versionData?.tree ?? []).map((entry) => ({
    value: entry.path,
    text: entry.path,
    link: `${window.location.origin}${basePath}/${versionPathSegment}/${entry.path}/`
  }));

  mappedOptions.sort((left, right) => compareVersionStrings(left.text, right.text));
  mappedOptions.unshift({ value: 'latest', text: 'latest', link: "/" });
  options.value = mappedOptions;
}

function onChange() {
  window.location.pathname = buildPathForVersion(window.location.pathname, selected.value);
}

function updateSelected() {
  let doReload = false;
  try {
    const newValue = extractVersionFromPath(window.location.pathname);
    doReload = newValue !== selected.value;
    selected.value = newValue;
  } catch (error) {
    doReload = 'latest' !== selected.value;
    selected.value = 'latest';
  } finally {
    if(doReload) window.location.reload(true);
  }
}

onMounted(async () => {
  await loadVersions();
  updateSelected()
});
</script>
