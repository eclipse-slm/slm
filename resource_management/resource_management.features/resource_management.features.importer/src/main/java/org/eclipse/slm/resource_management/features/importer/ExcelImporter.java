package org.eclipse.slm.resource_management.features.importer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dhatim.fastexcel.reader.ReadableWorkbook;
import org.eclipse.slm.resource_management.common.remote_access.ConnectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ExcelImporter {

    private static final Logger LOG = LoggerFactory.getLogger(ExcelImporter.class);

    public ImportDefinition importExcel(InputStream excelFileStream) {
        var importDefinition = new ImportDefinition();

        try (var workbook = new ReadableWorkbook(excelFileStream)) {

            var locationsSheet = workbook.getSheets()
                    .filter(s -> s.getName().equals("Locations")).findFirst().orElseThrow();
            try (var rows = locationsSheet.openStream()) {
                rows.skip(1).forEach(row -> {
                    var locationIdString = row.getCell(0).asString();
                    var locationId = UUID.fromString(locationIdString);
                    var locationName = row.getCell(1).asString();

                    importDefinition.addLocation(locationId, locationName);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            var capabilitiesSheet = workbook.getSheets()
                    .filter(s -> s.getName().equals("Capabilities")).findFirst().orElseThrow();
            try (var rows = capabilitiesSheet.openStream()) {
                rows.skip(1).forEach(row -> {
                    var capabilityIdString = row.getCell(0).asString();
                    if (capabilityIdString.isEmpty()) {
                        return;
                    }
                    var capabilityId = UUID.fromString(capabilityIdString);
                    var capabilityName = row.getCell(1).asString();

                    importDefinition.addCapability(capabilityName, capabilityId);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            var devicesSheet = workbook.getSheets()
                    .filter(s -> s.getName().equals("Devices")).findFirst().orElseThrow();
            var devices = new ArrayList<ImportDevice>();

            var columnNamesToIndex = new HashMap<String, Integer>();
            try (var rows = devicesSheet.openStream()) {
                rows.findFirst().ifPresent(row -> {
                    row.forEach(cell -> columnNamesToIndex.put(cell.getValue().toString().toLowerCase(), cell.getColumnIndex()));
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try (var rows = devicesSheet.openStream()) {
                rows.skip(1).forEach(row -> {
                    var name = row.getCellAsString(columnNamesToIndex.get("name")).get();
                    var assetId = row.getCellAsString(columnNamesToIndex.get("asset id")).orElseGet(() -> "");
                    var hostname = row.getCellAsString(columnNamesToIndex.get("hostname")).get();
                    var ipAddress = row.getCellAsString(columnNamesToIndex.get("ip address")).get();
                    var isResource = row.getCellAsString(columnNamesToIndex.get("is resource")).get();
                    var resourceIdString = row.getCellAsString(columnNamesToIndex.get("resource id")).get();
                    var resourceId = UUID.fromString(resourceIdString);
                    var firmwareVersion = row.getCellAsString(columnNamesToIndex.get("firmware version")).orElseGet(() -> "");
                    var connectionTypeString = row.getCellAsString(columnNamesToIndex.get("connection type")).get();
                    var connectionType = ConnectionType.valueOf(connectionTypeString);
                    var connectionPort = row.getCellAsNumber(columnNamesToIndex.get("connection port")).get().intValue();
                    var username = row.getCellAsString(columnNamesToIndex.get("username")).get();
                    var passwordCell = row.getCell(columnNamesToIndex.get("password"));
                    var password = passwordCell.getRawValue();
                    var locationIdString = row.getCellAsString(columnNamesToIndex.get("location id")).get();
                    var locationId = UUID.fromString(locationIdString);
                    var capabilities = new ArrayList<ImportCapability>();
                    try {
                        var capabilityNamesString = row.getCellAsString(columnNamesToIndex.get("capabilities")).orElseGet(() -> "");
                        var objectMapper = new ObjectMapper();
                        var capabilityNames = objectMapper.readValue(capabilityNamesString, new TypeReference<List<String>>(){});
                        for (var capabilityName : capabilityNames) {
                            boolean skipInstall = false;
                            if (capabilityName.contains(":")) {
                                var segments = capabilityName.split(":");
                                capabilityName = segments[0];
                                var capabilityOption = segments[segments.length - 1];
                                if (capabilityOption.equals("skip")) {
                                    skipInstall = true;
                                }
                            }
                            var capabilityId = importDefinition.getCapabilities().get(capabilityName);
                            if (capabilityId != null) {
                                capabilities.add(new ImportCapability(capabilityId, skipInstall));
                            }
                        }
                    } catch (Exception e) {
                        LOG.error("Failed to parse capabilities for device {}", name, e);
                    }

                    if (isResource.equals("yes")) {
                        var importDeviceBuilder = new ImportDevice.Builder()
                                .assetId(assetId)
                                .hostname(hostname)
                                .ipAddress(ipAddress)
                                .resourceId(resourceId)
                                .connectionType(connectionType)
                                .connectionPort(connectionPort)
                                .username(username)
                                .password(password)
                                .locationId(locationId)
                                .capabilities(capabilities);
                        if (!assetId.isEmpty()) {
                            importDeviceBuilder.assetId(assetId);
                        }
                        if (!firmwareVersion.isEmpty()) {
                            importDeviceBuilder.firmwareVersion(firmwareVersion);
                        }

                        devices.add(importDeviceBuilder.build());
                    } else {
                        LOG.info("Skipping device {} because it is not defined a resource", name);
                    }
                });
            }

            importDefinition.setDevices(devices);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to import Excel file");
        }

        return importDefinition;
    }

}
