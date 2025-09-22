package org.eclipse.slm.resource_management.features.device_integration.firmware_update;

import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJobEvent;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJob;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FirmwareUpdateJobStateMachineFactory {

    public final static Logger LOG = LoggerFactory.getLogger(FirmwareUpdateJobStateMachineFactory.class);

    private final FirmwareUpdateJobStateMachineInterceptor firmwareUpdateJobStateMachineInterceptor;

    public FirmwareUpdateJobStateMachineFactory(FirmwareUpdateJobStateMachineInterceptor firmwareUpdateJobStateMachineInterceptor) {
        this.firmwareUpdateJobStateMachineInterceptor = firmwareUpdateJobStateMachineInterceptor;
    }

    public StateMachine<FirmwareUpdateJobState, FirmwareUpdateJobEvent> create(FirmwareUpdateJob firmwareUpdateJob,
                                                                               FirmwareUpdateJobStateMachineListener firmwareUpdateJobStateMachineListener) throws Exception {
        var stateMachineBuilder = new StateMachineBuilder.Builder<FirmwareUpdateJobState, FirmwareUpdateJobEvent>();
        stateMachineBuilder.configureStates().withStates()
                .initial(FirmwareUpdateJobState.CREATED, context -> {
                    firmwareUpdateJobStateMachineListener.onStateEntry(firmwareUpdateJob, FirmwareUpdateJobState.CREATED);
                })

                .state(FirmwareUpdateJobState.PREPARING)
                .stateEntry(FirmwareUpdateJobState.PREPARING, context -> {
                    firmwareUpdateJobStateMachineListener.onStateEntry(firmwareUpdateJob, FirmwareUpdateJobState.PREPARING);
                })
                .state(FirmwareUpdateJobState.PREPARED)
                .stateEntry(FirmwareUpdateJobState.PREPARED, context -> {
                    firmwareUpdateJobStateMachineListener.onStateEntry(firmwareUpdateJob, FirmwareUpdateJobState.PREPARED);
                })

                .state(FirmwareUpdateJobState.ACTIVATING)
                .stateEntry(FirmwareUpdateJobState.ACTIVATING, context -> {
                    firmwareUpdateJobStateMachineListener.onStateEntry(firmwareUpdateJob, FirmwareUpdateJobState.ACTIVATING);
                })
                .state(FirmwareUpdateJobState.ACTIVATED)
                .stateEntry(FirmwareUpdateJobState.ACTIVATED, context -> {
                    firmwareUpdateJobStateMachineListener.onStateEntry(firmwareUpdateJob, FirmwareUpdateJobState.ACTIVATED);
                })

                .state(FirmwareUpdateJobState.CANCELING)
                .stateEntry(FirmwareUpdateJobState.CANCELING, context -> {
                    firmwareUpdateJobStateMachineListener.onStateEntry(firmwareUpdateJob, FirmwareUpdateJobState.CANCELING);
                })
                .state(FirmwareUpdateJobState.CANCELED)
                .stateEntry(FirmwareUpdateJobState.CANCELED, context -> {
                    firmwareUpdateJobStateMachineListener.onStateEntry(firmwareUpdateJob, FirmwareUpdateJobState.CANCELED);
                })

                .state(FirmwareUpdateJobState.FAILED)
                .stateEntry(FirmwareUpdateJobState.FAILED, context -> {
                    firmwareUpdateJobStateMachineListener.onStateEntry(firmwareUpdateJob, FirmwareUpdateJobState.FAILED);
                });

        for (var endState : FirmwareUpdateJobState.getEndStates()) {
            stateMachineBuilder.configureStates().withStates()
                .end(endState)
                .stateEntry(endState, context -> {
                    firmwareUpdateJobStateMachineListener.onStateEntry(firmwareUpdateJob, endState);
                });
        }

        stateMachineBuilder.configureTransitions()
                .withExternal()
                .source(FirmwareUpdateJobState.CREATED).target(FirmwareUpdateJobState.PREPARING).event(FirmwareUpdateJobEvent.PREPARATION_TRIGGERED)

            .and()
                .withExternal()
                .source(FirmwareUpdateJobState.PREPARING).target(FirmwareUpdateJobState.PREPARED).event(FirmwareUpdateJobEvent.PREPARATION_COMPLETED)
            .and()
                .withExternal()
                .source(FirmwareUpdateJobState.PREPARING).target(FirmwareUpdateJobState.FAILED).event(FirmwareUpdateJobEvent.PREPARATION_FAILED)

            .and()
                .withExternal()
                .source(FirmwareUpdateJobState.PREPARED).target(FirmwareUpdateJobState.ACTIVATING).event(FirmwareUpdateJobEvent.ACTIVATION_TRIGGERED)
            .and()
                .withExternal()
                .source(FirmwareUpdateJobState.PREPARED).target(FirmwareUpdateJobState.CANCELING).event(FirmwareUpdateJobEvent.CANCEL_TRIGGERED)

            .and()
                .withExternal()
                .source(FirmwareUpdateJobState.CANCELING).target(FirmwareUpdateJobState.CANCELED).event(FirmwareUpdateJobEvent.CANCEL_COMPLETED)
            .and()
                .withExternal()
                .source(FirmwareUpdateJobState.CANCELING).target(FirmwareUpdateJobState.FAILED).event(FirmwareUpdateJobEvent.CANCEL_FAILED)

            .and()
                .withExternal()
                .source(FirmwareUpdateJobState.ACTIVATING).target(FirmwareUpdateJobState.ACTIVATED).event(FirmwareUpdateJobEvent.ACTIVATION_COMPLETED)
            .and()
                .withExternal()
                .source(FirmwareUpdateJobState.ACTIVATING).target(FirmwareUpdateJobState.FAILED).event(FirmwareUpdateJobEvent.ACTIVATION_FAILED)
            .and()
                .withExternal()
                .source(FirmwareUpdateJobState.ACTIVATING).target(FirmwareUpdateJobState.CANCELING).event(FirmwareUpdateJobEvent.CANCEL_TRIGGERED);

    stateMachineBuilder.configureConfiguration().withConfiguration()
        .autoStartup(false)
        .listener(new StateMachineListenerAdapter<>() {
            @Override
            public void eventNotAccepted(Message<FirmwareUpdateJobEvent> event) {
                LOG.warn("Event not accepted: " + event);
            }

            @Override
            public void stateMachineError(StateMachine<FirmwareUpdateJobState, FirmwareUpdateJobEvent> stateMachine, Exception exception) {
                LOG.error("Error in firmware update state machine: " + exception.getMessage(), exception);
            }

        });

        var stateMachine = stateMachineBuilder.build();
        stateMachine.getStateMachineAccessor().doWithAllRegions( sma -> {
            sma.addStateMachineInterceptor(this.firmwareUpdateJobStateMachineInterceptor);
            sma.resetStateMachineReactively(new DefaultStateMachineContext<>(firmwareUpdateJob.getState(), null, null, null)).block();
        });
        stateMachine.startReactively().block();

        return stateMachine;
    }

}
