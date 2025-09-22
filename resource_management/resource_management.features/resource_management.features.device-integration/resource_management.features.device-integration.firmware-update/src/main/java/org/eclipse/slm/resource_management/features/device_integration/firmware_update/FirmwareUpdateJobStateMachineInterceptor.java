package org.eclipse.slm.resource_management.features.device_integration.firmware_update;

import org.eclipse.slm.resource_management.features.device_integration.firmware_update.messaging.FirmwareUpdateJobEventMessageSender;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJobEvent;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJob;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJobStateTransition;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJobState;
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
public class FirmwareUpdateJobStateMachineInterceptor implements StateMachineInterceptor<FirmwareUpdateJobState, FirmwareUpdateJobEvent> {

    public final static Logger LOG = LoggerFactory.getLogger(FirmwareUpdateJobStateMachineInterceptor.class);

    private final FirmwareUpdateJobJpaRepository firmwareUpdateJobJpaRepository;

    private final FirmwareUpdateJobStateTransitionJpaRepository firmwareUpdateJobStateTransitionJpaRepository;

    private final FirmwareUpdateJobEventMessageSender firmwareUpdateJobEventMessageSender;

    public FirmwareUpdateJobStateMachineInterceptor(FirmwareUpdateJobJpaRepository firmwareUpdateJobJpaRepository,
                                                    FirmwareUpdateJobStateTransitionJpaRepository firmwareUpdateJobStateTransitionJpaRepository,
                                                    FirmwareUpdateJobEventMessageSender firmwareUpdateJobEventMessageSender) {
        this.firmwareUpdateJobJpaRepository = firmwareUpdateJobJpaRepository;
        this.firmwareUpdateJobStateTransitionJpaRepository = firmwareUpdateJobStateTransitionJpaRepository;
        this.firmwareUpdateJobEventMessageSender = firmwareUpdateJobEventMessageSender;
    }

    @Override
    public Message<FirmwareUpdateJobEvent> preEvent(
            Message<FirmwareUpdateJobEvent> message,
            StateMachine<FirmwareUpdateJobState,
                    FirmwareUpdateJobEvent> stateMachine) {
        return message;
    }

    @Override
    public void preStateChange(State<FirmwareUpdateJobState, FirmwareUpdateJobEvent> state,
                               Message<FirmwareUpdateJobEvent> message,
                               Transition<FirmwareUpdateJobState,
                                       FirmwareUpdateJobEvent> transition,
                               StateMachine<FirmwareUpdateJobState,
                                       FirmwareUpdateJobEvent> stateMachine,
                               StateMachine<FirmwareUpdateJobState,
                                       FirmwareUpdateJobEvent> stateMachine1) {
    }

    @Override
    public void postStateChange(State<FirmwareUpdateJobState, FirmwareUpdateJobEvent> state,
                                Message<FirmwareUpdateJobEvent> message,
                                Transition<FirmwareUpdateJobState, FirmwareUpdateJobEvent> transition,
                                StateMachine<FirmwareUpdateJobState, FirmwareUpdateJobEvent> stateMachine,
                                StateMachine<FirmwareUpdateJobState, FirmwareUpdateJobEvent> stateMachine1) {

        Optional.ofNullable(message)
                .flatMap(msg -> Optional.ofNullable(msg.getHeaders().get("firmwareUpdateJob", FirmwareUpdateJob.class)))
                .ifPresent(firmwareUpdateJob -> {
                    LOG.debug("Updating firmware update job state to: {}", state.getId());
                    try {
                        firmwareUpdateJob.setState(state.getId());
                        this.firmwareUpdateJobJpaRepository.save(firmwareUpdateJob);

                        this.saveFirmwareUpdateJobStateTransition(
                                transition.getSource().getId(),
                                transition.getTarget().getId(),
                                firmwareUpdateJob);

                        firmwareUpdateJobEventMessageSender.sendMessage(firmwareUpdateJob);
                    } catch (Exception e) {
                        LOG.error("Error while updating firmware update process state: {}", e.getMessage(), e);
                    }
                });
    }

    @Override
    public StateContext<FirmwareUpdateJobState, FirmwareUpdateJobEvent> preTransition(
            StateContext<FirmwareUpdateJobState, FirmwareUpdateJobEvent> stateContext) {
        return stateContext;
    }

    @Override
    public StateContext<FirmwareUpdateJobState, FirmwareUpdateJobEvent> postTransition(
            StateContext<FirmwareUpdateJobState, FirmwareUpdateJobEvent> stateContext) {
        return stateContext;
    }

    @Override
    public Exception stateMachineError(
            StateMachine<FirmwareUpdateJobState, FirmwareUpdateJobEvent> stateMachine,
            Exception e) {
        return e;
    }

    private void saveFirmwareUpdateJobStateTransition(FirmwareUpdateJobState sourceState,
                                                      FirmwareUpdateJobState targetState,
                                                      FirmwareUpdateJob firmwareUpdateJob) {
        try {
            var jobStateTransition = new FirmwareUpdateJobStateTransition(sourceState, targetState, new Date(), firmwareUpdateJob);
            this.firmwareUpdateJobStateTransitionJpaRepository.save(jobStateTransition);
        } catch (Exception e) {
            LOG.error("Error while saving firmware update job state transition: {}", e.getMessage(), e);
        }
    }

}
