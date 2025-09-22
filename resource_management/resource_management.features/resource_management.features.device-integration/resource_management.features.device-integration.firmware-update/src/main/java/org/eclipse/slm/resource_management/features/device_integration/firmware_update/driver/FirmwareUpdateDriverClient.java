package org.eclipse.slm.resource_management.features.device_integration.firmware_update.driver;

import com.google.protobuf.ByteString;
import factory_x.artefact_update.v1.ArtefactUpdate;
import factory_x.artefact_update.v1.ArtefactUpdateApiGrpc;
import org.eclipse.slm.resource_management.features.device_integration.common.discovery.driver.AbstractDriverClient;
import org.eclipse.slm.resource_management.features.device_integration.common.discovery.driver.DriverInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class FirmwareUpdateDriverClient extends AbstractDriverClient {

    public final static Logger LOG = LoggerFactory.getLogger(FirmwareUpdateDriverClient.class);

    public static final int FIRMWARE_FILE_CHUNK_SIZE = 1024 * 1024; // 1 MB

    public FirmwareUpdateDriverClient(DriverInfo driverInfo) {
        super(driverInfo);
    }

    public void prepareFirmwareUpdate(UUID firmwareUpdateJobId, String connectionParametersBade64Encoded, byte[] firmwareUpdateFile, FirmwareUpdateJobListener firmwareUpdateJobListener) {

        var asyncStub = ArtefactUpdateApiGrpc.newStub(this.channel);

        var updateResponseStreamObserver = new FirmwareUpdateResponseStreamObserver(this.driverInfo, this.channel, firmwareUpdateJobId, FirmwareUpdateResponseStreamObserver.ActionType.PREPARE, firmwareUpdateFile);
        updateResponseStreamObserver.addListener(firmwareUpdateJobListener);

        var prepareRequestObserver = asyncStub.prepareUpdate(updateResponseStreamObserver);
        updateResponseStreamObserver.setRequestStreamObserver(prepareRequestObserver);

        ArtefactUpdate.ArtefactChunk chunkMetaData = this.buildMetadataArtefactChunk(firmwareUpdateJobId.toString(), connectionParametersBade64Encoded);
        prepareRequestObserver.onNext(chunkMetaData);
    }

    public void activateFirmwareUpdate(UUID firmwareUpdateJobId, String connectionParametersBade64Encoded, byte[] firmwareUpdateFile, FirmwareUpdateJobListener firmwareUpdateJobListener) {
        var asyncStub = ArtefactUpdateApiGrpc.newStub(this.channel);

        var updateResponseStreamObserver = new FirmwareUpdateResponseStreamObserver(this.driverInfo, this.channel, firmwareUpdateJobId, FirmwareUpdateResponseStreamObserver.ActionType.ACTIVATE, firmwareUpdateFile);
        updateResponseStreamObserver.addListener(firmwareUpdateJobListener);
        var activateRequestObserver = asyncStub.activateUpdate(updateResponseStreamObserver);

        ArtefactUpdate.ArtefactChunk chunkMetaData = this.buildMetadataArtefactChunk(firmwareUpdateJobId.toString(), connectionParametersBade64Encoded);
        activateRequestObserver.onNext(chunkMetaData);
    }

    private ArtefactUpdate.ArtefactChunk buildMetadataArtefactChunk(String firmwareUpdateJobId, String connectionParametersBade64Encoded) {
        var deviceIdentifierBlob = ByteString.copyFromUtf8(connectionParametersBade64Encoded);

        ArtefactUpdate.ArtefactChunk chunk = ArtefactUpdate.ArtefactChunk.newBuilder()
                .setMetadata(ArtefactUpdate.ArtefactMetaData.newBuilder()
                        .setJobIdentifier(ArtefactUpdate.JobIdentifier.newBuilder()
                                .setJobId(firmwareUpdateJobId)
                                .setJobIdBytes(ByteString.copyFromUtf8(firmwareUpdateJobId))
                                .build())
                        .setArtefactIdentifier(ArtefactUpdate.ArtefactIdentifier.newBuilder()
                                .setType(ArtefactUpdate.ArtefactType.AT_FIRMWARE)
                                .build()
                        )
                        .setDeviceIdentifier(ArtefactUpdate.DeviceIdentifier.newBuilder()
                                .setBlob(deviceIdentifierBlob)
                                .build())
                        .build()
                )
                .build();

        return chunk;
    }

}
