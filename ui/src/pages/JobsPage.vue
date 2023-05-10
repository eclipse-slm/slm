<template>
  <jobs-overview />
</template>

<script>
  import JobsOverview from '@/components/jobs/JobsOverview'

  export default {
    components: {
      JobsOverview,
    },
    created () {
      this.$store.dispatch('updateJobsStore')
    },
  }
</script>
