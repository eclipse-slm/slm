import {defineStore} from "pinia";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";

export interface JobsStoreState{
  jobs_: any[],
  jobs_running_: any[],
  timeoutObject:  NodeJS.Timeout | null,
}

export const useJobsStore = defineStore('jobsStore', {
  persist: true,
  state: (): JobsStoreState =>({
    jobs_: [],
    jobs_running_: [],
    timeoutObject:  null,
  }),
  getters: {
    jobs: (state) => {
      return state.jobs_
    },
    jobs_running: (state) => {
      return state.jobs_running_
    },
  },
  actions:{
    async updateJobsStore () {

      ResourceManagementClient.jobApi.getJobs().then(result => {
          if (result.data) {
            const jobs = <any[]>result.data;
            this.jobs_ = jobs;

            if (jobs.filter(job => job.status === 'running').length > 0) {
              this.timeoutObject = setInterval(() => {
                this.calculateElapsed();
              }, 1000)
            } else {
              if(this.timeoutObject){
                clearInterval(this.timeoutObject)
              }
            }
          }

          },
      ).catch(logRequestError)
    },
    calculateElapsed () {
      this.jobs_running_.forEach(job => {
        const elapsed = new Date().getTime() - new Date(job.started).getTime()
        this.jobs_running_.find(j => j.id === job.id).elapsed = elapsed;
      })
    },
  }
});

