package org.eclipse.slm.resource_management.features.capabilities.jobs;

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
public class CapabilityJobStateMachineFactory {

    public final static Logger LOG = LoggerFactory.getLogger(CapabilityJobStateMachineFactory.class);

    private final CapabilityJobStateMachineInterceptor firmwareUpdateJobStateMachineInterceptor;

    public CapabilityJobStateMachineFactory(CapabilityJobStateMachineInterceptor firmwareUpdateJobStateMachineInterceptor) {
        this.firmwareUpdateJobStateMachineInterceptor = firmwareUpdateJobStateMachineInterceptor;
    }

    public StateMachine<CapabilityJobState, CapabilityJobEvent> create(CapabilityJob capabilityJob,
                                                                       CapabilityJobStateMachineListener capabilityJobStateMachineListener) throws Exception {
        var stateMachineBuilder = new StateMachineBuilder.Builder<CapabilityJobState, CapabilityJobEvent>();
        stateMachineBuilder.configureStates().withStates()
                .initial(CapabilityJobState.CREATED, context -> {
                    capabilityJobStateMachineListener.onStateEntry(capabilityJob, CapabilityJobState.CREATED);
                })

                .state(CapabilityJobState.INSTALLING)
                .stateEntry(CapabilityJobState.INSTALLING, context -> {
                    capabilityJobStateMachineListener.onStateEntry(capabilityJob, CapabilityJobState.INSTALLING);
                })
                .state(CapabilityJobState.INSTALLED)
                .stateEntry(CapabilityJobState.INSTALLED, context -> {
                    capabilityJobStateMachineListener.onStateEntry(capabilityJob, CapabilityJobState.INSTALLED);
                })

                .state(CapabilityJobState.UNINSTALLING)
                .stateEntry(CapabilityJobState.UNINSTALLING, context -> {
                    capabilityJobStateMachineListener.onStateEntry(capabilityJob, CapabilityJobState.UNINSTALLING);
                })
                .state(CapabilityJobState.UNINSTALLED)
                .stateEntry(CapabilityJobState.UNINSTALLED, context -> {
                    capabilityJobStateMachineListener.onStateEntry(capabilityJob, CapabilityJobState.UNINSTALLED);
                })

                .state(CapabilityJobState.FAILED)
                .stateEntry(CapabilityJobState.FAILED, context -> {
                    capabilityJobStateMachineListener.onStateEntry(capabilityJob, CapabilityJobState.FAILED);
                });

        for (var endState : CapabilityJobState.getEndStates()) {
            stateMachineBuilder.configureStates().withStates()
                .end(endState)
                .stateEntry(endState, context -> {
                    capabilityJobStateMachineListener.onStateEntry(capabilityJob, endState);
                });
        }

        stateMachineBuilder.configureTransitions()
                .withExternal()
                .source(CapabilityJobState.CREATED).target(CapabilityJobState.INSTALLING).event(CapabilityJobEvent.INSTALL_TRIGGERED)
            .and()
                .withExternal()
                .source(CapabilityJobState.CREATED).target(CapabilityJobState.INSTALLED).event(CapabilityJobEvent.INSTALL_SKIPPED)
            .and()
                .withExternal()
                .source(CapabilityJobState.CREATED).target(CapabilityJobState.FAILED).event(CapabilityJobEvent.ERROR_OCCURRED)

            .and()
                .withExternal()
                .source(CapabilityJobState.INSTALLING).target(CapabilityJobState.INSTALLED).event(CapabilityJobEvent.INSTALL_COMPLETED)
            .and()
                .withExternal()
                .source(CapabilityJobState.INSTALLING).target(CapabilityJobState.FAILED).event(CapabilityJobEvent.ERROR_OCCURRED)

            .and()
                .withExternal()
                .source(CapabilityJobState.INSTALLED).target(CapabilityJobState.UNINSTALLING).event(CapabilityJobEvent.UNINSTALL_TRIGGERED)
            .and()
                .withExternal()
                .source(CapabilityJobState.INSTALLED).target(CapabilityJobState.UNINSTALLED).event(CapabilityJobEvent.UNINSTALL_SKIPPED)
            .and()
                .withExternal()
                .source(CapabilityJobState.INSTALLED).target(CapabilityJobState.FAILED).event(CapabilityJobEvent.ERROR_OCCURRED)

            .and()
                .withExternal()
                .source(CapabilityJobState.UNINSTALLING).target(CapabilityJobState.UNINSTALLED).event(CapabilityJobEvent.UNINSTALL_COMPLETED)
            .and()
                .withExternal()
                .source(CapabilityJobState.UNINSTALLING).target(CapabilityJobState.FAILED).event(CapabilityJobEvent.ERROR_OCCURRED);

    stateMachineBuilder.configureConfiguration().withConfiguration()
        .autoStartup(false)
        .listener(new StateMachineListenerAdapter<>() {
            @Override
            public void eventNotAccepted(Message<CapabilityJobEvent> event) {
                LOG.warn("Event not accepted: " + event);
            }

            @Override
            public void stateMachineError(StateMachine<CapabilityJobState, CapabilityJobEvent> stateMachine, Exception exception) {
                LOG.error("Error in capability job state machine: " + exception.getMessage(), exception);
            }

        });

        var stateMachine = stateMachineBuilder.build();
        stateMachine.getStateMachineAccessor().doWithAllRegions( sma -> {
            sma.addStateMachineInterceptor(this.firmwareUpdateJobStateMachineInterceptor);
            sma.resetStateMachineReactively(new DefaultStateMachineContext<>(capabilityJob.getState(), null, null, null)).block();
        });
        stateMachine.startReactively().block();

        return stateMachine;
    }

}
