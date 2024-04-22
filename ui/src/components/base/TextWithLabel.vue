<template>
  <div>
    <div class="text-grey mb-0 text-caption">
      {{ label }}
    </div>
    <div>
      {{ text }}
    </div>
  </div>
</template>

<script>
  export default {
    name: 'TextWithLabel',
    props: ['label', 'text'],
  }
</script>
