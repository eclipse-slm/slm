package org.eclipse.slm.common.awx.client.test.unittests;

import org.eclipse.slm.common.awx.client.observer.AwxJobObserver;
import org.eclipse.slm.common.awx.client.observer.IAwxJobObserverListener;
import org.eclipse.slm.notification_service.model.JobFinalState;
import org.eclipse.slm.notification_service.model.JobGoal;
import org.eclipse.slm.notification_service.model.JobState;
import org.eclipse.slm.notification_service.model.JobTarget;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AwxJobObserverTests {

    @Test
    public void observerShouldInformListenersWhenStateOfObservedJobChanged() {
        var listener = new TestAwxJobObserverListener();
        var observer = new AwxJobObserver(listener);
        observer.observeJob(0, JobTarget.PROJECT, JobGoal.CREATE);
        observer.check(0, JobState.RUNNING.toString().toLowerCase());
        observer.check(0, JobFinalState.SUCCESSFUL.toString().toLowerCase());

        assertTrue(listener.onJobStateChanged);
        assertTrue(listener.onJobStateFinished);
    }

    @Test
    public void observerShouldNotInformListenersWhenStateOfOtherJobChanged() {
        var listener = new TestAwxJobObserverListener();
        var observer = new AwxJobObserver(listener);
        observer.observeJob(0, JobTarget.PROJECT, JobGoal.CREATE);
        observer.check(1, JobFinalState.SUCCESSFUL.toString().toLowerCase());
        observer.check(1, JobFinalState.SUCCESSFUL.toString().toLowerCase());

        assertFalse(listener.onJobStateChanged);
        assertFalse(listener.onJobStateFinished);
    }

    private final class TestAwxJobObserverListener implements IAwxJobObserverListener{
        public boolean onJobStateChanged = false;
        public boolean onJobStateFinished = false;

        @Override
        public void onJobStateChanged(AwxJobObserver sender, JobState newState) {
            this.onJobStateChanged = true;
        }

        @Override
        public void onJobStateFinished(AwxJobObserver sender, JobFinalState finalState) {
            this.onJobStateFinished = true;
        }
    }

}
