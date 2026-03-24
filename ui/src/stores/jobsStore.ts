import {defineStore} from "pinia";
import ResourceManagementClient from "@/api/resource-management/resource-management-client";
import logRequestError from "@/api/restApiHelper";
import ApiState from "@/api/apiState";

export interface JobsStoreState{
  apiState: number,
  jobs: any[],
  jobs_running: any[],
  timeoutObject:  NodeJS.Timeout | null,
}

export const useJobsStore = defineStore('jobsStore', {
    persist: true,

    state: (): JobsStoreState => ({
        apiState: ApiState.INIT,
        jobs: [],
        jobs_running: [],
        timeoutObject: null,
    }),

    getters: {},

    actions: {
        async getJobs() {
            ResourceManagementClient.jobApi.getJobs().then(result => {
                if (result.data) {
                    const jobs = <any[]>result.data;
                    this.jobs = jobs;

                    if (this.jobs && this.jobs.length > 0) {
                        if (jobs.filter(job => job.status === 'running').length > 0) {
                            this.timeoutObject = setInterval(() => {
                                this.calculateElapsed();
                            }, 1000)
                        } else {
                            if (this.timeoutObject) {
                                clearInterval(this.timeoutObject)
                            }
                        }
                    }
                }
            }).catch(logRequestError)
        },

        calculateElapsed() {
            this.jobs_running.forEach(job => {
                const elapsed = new Date().getTime() - new Date(job.started).getTime()
                this.jobs_running.find(j => j.id === job.id).elapsed = elapsed;
            })
        },

        async updateStore() {
            if (this.apiState === ApiState.INIT) {
                this.apiState = ApiState.LOADING;
            } else {
                this.apiState = ApiState.UPDATING;
            }

            return Promise.all([
                this.getJobs(),
            ]).then(() => {
                this.apiState = ApiState.LOADED;
                console.log("jobsStore updated")
            }).catch((e) => {
                this.apiState = ApiState.ERROR;
                console.log("Failed to update jobsStore: ", e)
            });
        },
    }
});

