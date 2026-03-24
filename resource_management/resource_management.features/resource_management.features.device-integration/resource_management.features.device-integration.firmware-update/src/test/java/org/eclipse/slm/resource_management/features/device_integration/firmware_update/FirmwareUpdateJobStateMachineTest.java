package org.eclipse.slm.resource_management.features.device_integration.firmware_update;

import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJob;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJobEvent;
import org.eclipse.slm.resource_management.features.device_integration.firmware_update.model.FirmwareUpdateJobState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.statemachine.StateMachine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Disabled
public class FirmwareUpdateJobStateMachineTest {

    private FirmwareUpdateJobStateMachineFactory factory;
    private FirmwareUpdateJobStateMachineListener listener;
    private FirmwareUpdateJobStateMachineInterceptor interceptor;
    private FirmwareUpdateJob job;

    @BeforeEach
    void setUp() {
        interceptor = mock(FirmwareUpdateJobStateMachineInterceptor.class);
        listener = mock(FirmwareUpdateJobStateMachineListener.class);
        factory = new FirmwareUpdateJobStateMachineFactory(interceptor);
        job = mock(FirmwareUpdateJob.class);
        when(job.getState()).thenReturn(FirmwareUpdateJobState.CREATED);
    }

    @Test
    void testHappyPath() throws Exception {
        StateMachine<FirmwareUpdateJobState, FirmwareUpdateJobEvent> sm = factory.create(job, listener);

        assertEquals(FirmwareUpdateJobState.CREATED, sm.getState().getId());

        sm.sendEvent(FirmwareUpdateJobEvent.PREPARATION_TRIGGERED);
        assertEquals(FirmwareUpdateJobState.PREPARING, sm.getState().getId());

        sm.sendEvent(FirmwareUpdateJobEvent.PREPARATION_COMPLETED);
        assertEquals(FirmwareUpdateJobState.PREPARED, sm.getState().getId());

        sm.sendEvent(FirmwareUpdateJobEvent.ACTIVATION_TRIGGERED);
        assertEquals(FirmwareUpdateJobState.ACTIVATING, sm.getState().getId());

        sm.sendEvent(FirmwareUpdateJobEvent.ACTIVATION_COMPLETED);
        assertEquals(FirmwareUpdateJobState.ACTIVATED, sm.getState().getId());
    }

    @Test
    void testPreparationFailed() throws Exception {
        StateMachine<FirmwareUpdateJobState, FirmwareUpdateJobEvent> sm = factory.create(job, listener);

        sm.sendEvent(FirmwareUpdateJobEvent.PREPARATION_TRIGGERED);
        sm.sendEvent(FirmwareUpdateJobEvent.PREPARATION_FAILED);

        assertEquals(FirmwareUpdateJobState.FAILED, sm.getState().getId());
    }

    @Test
    void testCancelFlow() throws Exception {
        StateMachine<FirmwareUpdateJobState, FirmwareUpdateJobEvent> sm = factory.create(job, listener);

        sm.sendEvent(FirmwareUpdateJobEvent.PREPARATION_TRIGGERED);
        sm.sendEvent(FirmwareUpdateJobEvent.PREPARATION_COMPLETED);
        sm.sendEvent(FirmwareUpdateJobEvent.CANCEL_TRIGGERED);
        assertEquals(FirmwareUpdateJobState.CANCELING, sm.getState().getId());

        sm.sendEvent(FirmwareUpdateJobEvent.CANCEL_COMPLETED);
        assertEquals(FirmwareUpdateJobState.CANCELED, sm.getState().getId());
    }

    @Test
    void testActivationFailed() throws Exception {
        StateMachine<FirmwareUpdateJobState, FirmwareUpdateJobEvent> sm = factory.create(job, listener);

        sm.sendEvent(FirmwareUpdateJobEvent.PREPARATION_TRIGGERED);
        sm.sendEvent(FirmwareUpdateJobEvent.PREPARATION_COMPLETED);
        sm.sendEvent(FirmwareUpdateJobEvent.ACTIVATION_TRIGGERED);
        sm.sendEvent(FirmwareUpdateJobEvent.ACTIVATION_FAILED);

        assertEquals(FirmwareUpdateJobState.FAILED, sm.getState().getId());
    }

}
