package org.eclipse.slm.resource_management.features.device_integration.firmware_update.driver;

import com.google.protobuf.ByteString;
import factory_x.artefact_update.v1.ArtefactUpdate;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.eclipse.slm.resource_management.features.device_integration.common.discovery.driver.DriverInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.eclipse.slm.resource_management.features.device_integration.firmware_update.driver.FirmwareUpdateDriverClient.FIRMWARE_FILE_CHUNK_SIZE;

public class FirmwareUpdateResponseStreamObserver implements StreamObserver<ArtefactUpdate.ArtefactMessage> {

    public final static Logger LOG = LoggerFactory.getLogger(FirmwareUpdateResponseStreamObserver.class);

    private final DriverInfo driverInfo;

    private final ManagedChannel channel;

    private final List<FirmwareUpdateJobListener> listeners = new ArrayList<>();

    private final UUID firmwareUpdateJobId;

    private final byte[] firmwareUpdateFile;

    private final ActionType actionType;

    private StreamObserver<ArtefactUpdate.ArtefactChunk> requestStreamObserver;

    public void setRequestStreamObserver(StreamObserver<ArtefactUpdate.ArtefactChunk> requestStreamObserver) {
        this.requestStreamObserver = requestStreamObserver;
    }

    public enum ActionType {
        PREPARE,
        ACTIVATE
    }

    public FirmwareUpdateResponseStreamObserver(DriverInfo driverInfo,
                                                ManagedChannel channel,
                                                UUID firmwareUpdateJobId,
                                                ActionType actionType,
                                                byte[] firmwareUpdateFile
    ) {
        this.driverInfo = driverInfo;
        this.channel = channel;
        this.firmwareUpdateJobId = firmwareUpdateJobId;
        this.firmwareUpdateFile = firmwareUpdateFile;
        this.actionType = actionType;
    }


    @Override
    public void onNext(ArtefactUpdate.ArtefactMessage artefactMessage) {

        switch (artefactMessage.getMessageCase()) {
            case STATUS -> {
                var phase = artefactMessage.getStatus().getPhase();
                var message = artefactMessage.getStatus().getMessage();
                var progress = artefactMessage.getStatus().getProgress();

                LOG.debug("Update job '{}' of driver '{}' is in phase '{}' with progress '{} %': {}", this.firmwareUpdateJobId, this.driverInfo, phase, message, progress);

                for (var listener : listeners) {
                    listener.onUpdateMessage(this.firmwareUpdateJobId, new Date(), message, phase.name(), progress);
                }
            }

            case REQUEST -> {
                switch (artefactMessage.getRequest().getType()) {
                    case AORT_ARTEFACT_TRANSMISSION -> {
                        int offset = 0;
                        while (offset < firmwareUpdateFile.length) {
                            int end = Math.min(offset + FirmwareUpdateDriverClient.FIRMWARE_FILE_CHUNK_SIZE, firmwareUpdateFile.length);
                            byte[] chunkData = java.util.Arrays.copyOfRange(firmwareUpdateFile, offset, end);
                            var chunkByteString = ByteString.copyFrom(chunkData);

                            var chunkFilePart = ArtefactUpdate.ArtefactChunk.newBuilder()
                                    .setFileContent(chunkByteString)
                                    .build();

                            this.requestStreamObserver.onNext(chunkFilePart);
                            offset = end;
                        }

                        this.requestStreamObserver.onCompleted();
                    }

                    default -> {
                        var errorMessage = "Unknown request type: " + artefactMessage.getRequest().getType();
                        LOG.error(errorMessage);
                        this.onError(new RuntimeException(errorMessage));
                    }
                }
            }

            case MESSAGE_NOT_SET -> {

            }
        }

        LOG.debug("Received message for update job '{}' for driver '{}': {}", this.firmwareUpdateJobId, this.driverInfo, artefactMessage);
    }

    @Override
    public void onError(Throwable throwable) {
        LOG.error("Error during update job '{}' for driver '{}'", this.firmwareUpdateJobId, this.driverInfo, throwable);

        var errorMessage = "Error during update job '" + this.firmwareUpdateJobId + "' for driver '" + this.driverInfo + "': " + throwable.getMessage();
        for (var listener : listeners) {
            listener.onUpdateFailed(this.firmwareUpdateJobId, errorMessage);
        }

        this.channel.shutdown();
    }

    @Override
    public void onCompleted() {
        switch (this.actionType) {
            case PREPARE -> {
                LOG.debug("Firmware update job '{}' prepared for driver '{}'", this.firmwareUpdateJobId, this.driverInfo);
                for (var listener : listeners) {
                    listener.onUpdatePrepared(this.firmwareUpdateJobId);
                }
            }
            case ACTIVATE -> {
                LOG.debug("Firmware update job '{}' activated for driver '{}'", this.firmwareUpdateJobId, this.driverInfo);
                for (var listener : listeners) {
                    listener.onUpdateActivated(this.firmwareUpdateJobId);
                }
            }
            default -> {
                LOG.error("Unknown action type: {}", this.actionType);
            }
        }

        this.channel.shutdown();
    }

    public void addListener(FirmwareUpdateJobListener listener) {
        this.listeners.add(listener);
    }
}
