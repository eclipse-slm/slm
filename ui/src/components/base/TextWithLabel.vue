<template>
  <div>
    <div class="grey--text mb-0 text-caption">
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
