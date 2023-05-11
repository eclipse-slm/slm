---
permalink: /docs/development/debugging/
---

# Debugging

## Failing Jobs
An overview of all jobs can be found in the `Jobs` section. The status column can determine whether a job failed or was successful. If a job has failed, more details can often be found in AWX. 

![Jobs Overview](/img/figures/development/failing-jobs-slm-overview.png)

### AWX

First of all, the job id of the failed job which can be found in the `Jobs` section is required. Clicking on the job id opens a new tab with the job detail view of the corresponding job in AWX.
![Jobs Overview - Get Job ID](/img/figures/development/failing-jobs-slm-overview-get-job-id.png)

Alternatively, log in to the AWX Web UI. It is accessible by default on port 80 on the host where Service Lifecycle Management is installed. The credentials are admin / password by default. Then select the `Jobs` section and search for the job you want to get more details about. 
![AWX Jobs Overview](/img/figures/development/failing-jobs-awx-overview.png)

In the details view, the job details can be found on the left side and the log output on the right side. In the upper right corner, the full log file can be downloaded. When contacting the developers regarding a problem, attach the downloaded log file and a screenshot of the job view.
![AWX Job Details](/img/figures/development/failing-jobs-awx-job-details.png)