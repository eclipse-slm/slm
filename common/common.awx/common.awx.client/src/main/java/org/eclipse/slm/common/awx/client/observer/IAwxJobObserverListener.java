package org.eclipse.slm.common.awx.client.observer;

import org.eclipse.slm.notification_service.model.JobFinalState;
import org.eclipse.slm.notification_service.model.JobState;

public interface IAwxJobObserverListener {

    void onJobStateChanged(AwxJobObserver sender, JobState newState);

    void onJobStateFinished(AwxJobObserver sender, JobFinalState finalState);

}
