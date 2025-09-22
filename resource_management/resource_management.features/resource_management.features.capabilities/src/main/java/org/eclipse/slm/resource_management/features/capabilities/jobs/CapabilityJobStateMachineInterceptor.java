package org.eclipse.slm.resource_management.features.capabilities.jobs;

import org.eclipse.slm.resource_management.features.capabilities.jobs.messaging.CapabilityJobEventMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class CapabilityJobStateMachineInterceptor implements StateMachineInterceptor<CapabilityJobState, CapabilityJobEvent> {

    public final static Logger LOG = LoggerFactory.getLogger(CapabilityJobStateMachineInterceptor.class);

    private final CapabilityJobJpaRepository capabilityJobJpaRepository;

    private final CapabilityJobStateTransitionJpaRepository capabilityJobStateTransitionJpaRepository;

    private final CapabilityJobEventMessageSender capabilityJobEventMessageSender;

    public CapabilityJobStateMachineInterceptor(CapabilityJobJpaRepository capabilityJobJpaRepository,
                                                CapabilityJobStateTransitionJpaRepository capabilityJobStateTransitionJpaRepository,
                                                CapabilityJobEventMessageSender capabilityJobEventMessageSender) {
        this.capabilityJobJpaRepository = capabilityJobJpaRepository;
        this.capabilityJobStateTransitionJpaRepository = capabilityJobStateTransitionJpaRepository;
        this.capabilityJobEventMessageSender = capabilityJobEventMessageSender;
    }

    @Override
    public Message<CapabilityJobEvent> preEvent(
            Message<CapabilityJobEvent> message,
            StateMachine<CapabilityJobState,
                    CapabilityJobEvent> stateMachine) {
        return message;
    }

    @Override
    public void preStateChange(State<CapabilityJobState, CapabilityJobEvent> state,
                               Message<CapabilityJobEvent> message,
                               Transition<CapabilityJobState,
                                       CapabilityJobEvent> transition,
                               StateMachine<CapabilityJobState,
                                       CapabilityJobEvent> stateMachine,
                               StateMachine<CapabilityJobState,
                                       CapabilityJobEvent> stateMachine1) {
    }

    @Override
    public void postStateChange(State<CapabilityJobState, CapabilityJobEvent> state,
                                Message<CapabilityJobEvent> message,
                                Transition<CapabilityJobState, CapabilityJobEvent> transition,
                                StateMachine<CapabilityJobState, CapabilityJobEvent> stateMachine,
                                StateMachine<CapabilityJobState, CapabilityJobEvent> stateMachine1) {

        Optional.ofNullable(message)
                .flatMap(msg -> Optional.ofNullable(msg.getHeaders().get("capabilityJob", CapabilityJob.class)))
                .ifPresent(capabilityJob -> {
                    LOG.debug("Updating firmware update job state to: {}", state.getId());
                    try {
                        capabilityJob.setState(state.getId());
                        this.capabilityJobJpaRepository.save(capabilityJob);

                        this.saveCapabilityJobStateTransition(
                                transition.getSource().getId(),
                                transition.getTarget().getId(),
                                capabilityJob);

                        capabilityJobEventMessageSender.sendMessage(capabilityJob);
                    } catch (Exception e) {
                        LOG.error("Error while updating firmware update process state: {}", e.getMessage(), e);
                    }
                });
    }

    @Override
    public StateContext<CapabilityJobState, CapabilityJobEvent> preTransition(
            StateContext<CapabilityJobState, CapabilityJobEvent> stateContext) {
        return stateContext;
    }

    @Override
    public StateContext<CapabilityJobState, CapabilityJobEvent> postTransition(
            StateContext<CapabilityJobState, CapabilityJobEvent> stateContext) {
        return stateContext;
    }

    @Override
    public Exception stateMachineError(
            StateMachine<CapabilityJobState, CapabilityJobEvent> stateMachine,
            Exception e) {
        return e;
    }

    private void saveCapabilityJobStateTransition(CapabilityJobState sourceState,
                                                  CapabilityJobState targetState,
                                                  CapabilityJob capabilityJob) {
        try {
            var jobStateTransition = new CapabilityJobStateTransition(sourceState, targetState, new Date(), capabilityJob);
            this.capabilityJobStateTransitionJpaRepository.save(jobStateTransition);
        } catch (Exception e) {
            LOG.error("Error while saving capability job state transition: {}", e.getMessage(), e);
        }
    }

}
