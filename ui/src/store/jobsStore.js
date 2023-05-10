import JobsRestApi from '@/api/resource-management/jobsRestApi'

export default {
  state: {
    jobs: [],
    jobs_running: [],
    timeoutObject: null,
  },

  getters: {
    jobs: (state) => {
      return state.jobs
    },
    jobs_running: (state) => {
      return state.jobs_running
    },
  },

  actions: {
    async updateJobsStore (context) {
      JobsRestApi.getJobs().then(jobs => {
            context.commit('SET_JOBS', jobs)

            if (jobs.filter(job => job.status === 'running').length > 0) {
              context.state.timeoutObject = setInterval(() => {
                context.dispatch('calculateElapsed')
              }, 1000)
            } else {
              clearInterval(context.state.timeoutObject)
            }
          },
      )
    },
    calculateElapsed (context) {
      context.state.jobs_running.forEach(job => {
        const elapsed = new Date() - new Date(job.started)

        context.commit('UPDATE_JOB_ELAPSED', { id: job.id, elapsed: elapsed })
      })
    },
  },

  mutations: {
    SET_JOBS (state, jobs) {
      state.jobs = jobs
      state.jobs_running = jobs.filter(job => job.status === 'running')
    },
    UPDATE_JOB_ELAPSED (state, payload) {
      state.jobs_running.find(job => job.id === payload.id).elapsed = payload.elapsed
    },
  },
}
