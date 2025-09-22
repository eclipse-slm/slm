package org.eclipse.slm.resource_management.common.remote_access;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConnectionTypeUtils {
    public static boolean isRemote(ConnectionType connectionType) {
        return connectionType.getRemoteAccess();
    }

    public static List<ConnectionType> getRemoteConnectionTypes() {
        return Arrays.stream(ConnectionType.values())
                .filter(ct -> ct.getRemoteAccess())
                .collect(Collectors.toList());
    }

    public static List<ConnectionTypeDTO> getRemoteConnectionTypeDTOs() {
        List<ConnectionTypeDTO> connectionTypeDTOs = new ArrayList<>();
        getRemoteConnectionTypes().stream().forEach(t -> connectionTypeDTOs.add(new ConnectionTypeDTO(t)));
        return connectionTypeDTOs;
    }

    public static List<ConnectionType> getNonRemoteConnectionTypes() {
        return Arrays.stream(ConnectionType.values())
                .filter(ct -> !ct.getRemoteAccess())
                .collect(Collectors.toList());
    }
}
