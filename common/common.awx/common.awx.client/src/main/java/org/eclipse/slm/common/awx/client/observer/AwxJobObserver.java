package org.eclipse.slm.common.awx.client.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AwxJobObserver {
    public final static Logger LOG = LoggerFactory.getLogger(AwxJobObserver.class);

    public int jobId;
    public JobTarget jobTarget;
    public JobGoal jobGoal;

    private AwxJobEndpoint awxJobEndpoint = null;

    private List<IAwxJobObserverListener> jobObserverListeners = new ArrayList<>();

    private final List<String> finalStates = Stream.of(JobFinalState.values())
            .map(JobFinalState::name)
            .map(String::toLowerCase)
            .collect(Collectors.toList());
    private final List<String> states = Stream.of(JobState.values())
            .map(JobState::name)
            .map(String::toLowerCase)
            .collect(Collectors.toList());


    public AwxJobObserver(int jobId, JobTarget jobTarget, JobGoal jobGoal, IAwxJobObserverListener jobObserverListener) {
        this.observeJob(jobId, jobTarget, jobGoal);
        this.addListener(jobObserverListener);
    }

    public AwxJobObserver(IAwxJobObserverListener listener) {
        this.addListener(listener);
    }

    public AwxJobObserver(
            int jobId,
            JobTarget jobTarget,
            JobGoal jobGoal
    ) {
        this.observeJob(jobId, jobTarget, jobGoal);
    }

    public void observeJob(int jobId, JobTarget jobTarget, JobGoal jobGoal) {
        this.jobId = jobId;
        this.jobTarget = jobTarget;
        this.jobGoal = jobGoal;
    }

    public void check(int jobId, String jobStatusString) {
        if (this.jobId == jobId) {
            if (finalStates.contains(jobStatusString)) {
                var finalState = JobFinalState.valueOf(jobStatusString.toUpperCase());
                fireOnJobFinished(finalState);
            } else if (states.contains(jobStatusString)) {
                states.remove(jobStatusString);

                var jobStatus = JobState.valueOf(jobStatusString.toUpperCase());
                fireOnJobStateChanged(jobStatus);
            }
        }

    }

    public void addListener(IAwxJobObserverListener listener) {
        this.jobObserverListeners.add(listener);
    }

    public void removeListener(IAwxJobObserverListener listener) {
        this.jobObserverListeners.remove(listener);
    }

    public void fireOnJobStateChanged(JobState newState) {
        for (var listener : new ArrayList<>(this.jobObserverListeners)) {
            listener.onJobStateChanged(this, newState);
        }
    }

    public void fireOnJobFinished(JobFinalState finalState) {
        this.stopListenToEndpoint();
        for (var listener : new ArrayList<>(this.jobObserverListeners)) {
            listener.onJobStateFinished(this, finalState);
        }
    }

    public void listenToEndpoint(AwxJobEndpoint awxJobEndpoint) {
        if (this.awxJobEndpoint != null) {
            this.stopListenToEndpoint();
        }
        this.awxJobEndpoint = awxJobEndpoint;
        this.awxJobEndpoint.registerObserver(this);
    }

    public void stopListenToEndpoint() {
        if (this.awxJobEndpoint != null) {
            this.awxJobEndpoint.removeObserver(this);
            this.awxJobEndpoint = null;
        }
    }

}
