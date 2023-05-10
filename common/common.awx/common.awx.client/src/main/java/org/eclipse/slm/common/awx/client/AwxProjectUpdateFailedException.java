package org.eclipse.slm.common.awx.client;

import org.eclipse.slm.common.awx.model.JobUpdate;

public class AwxProjectUpdateFailedException extends Throwable {
    String errMsg;
    JobUpdate jobUpdate;
    public AwxProjectUpdateFailedException(String s, JobUpdate jobUpdate) {
        this.errMsg = s;
        this.jobUpdate = jobUpdate;
    }
}
