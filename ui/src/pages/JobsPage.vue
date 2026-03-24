<template>
  <jobs-overview />
</template>

<script>
import JobsOverview from '@/components/jobs/JobsOverview'
import {useJobsStore} from "@/stores/jobsStore";

export default {
    components: {
      JobsOverview,
    },
    created () {
      const jobStore = useJobsStore();
      jobStore.updateStore();
    },
  }
</script>
