package org.eclipse.slm.resource_management.features.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipImporter {

    private static final Logger LOG = LoggerFactory.getLogger(ZipImporter.class);

    public ImportDefinition importZip(InputStream zipFileStream) {
        var importDefinition = new ImportDefinition();

        var aasxFilesOfResources = new HashMap<UUID, List<InputStream>>();
        try (var zipInputStream = new ZipInputStream(zipFileStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                // Check if entry is a directory
                if (!entry.isDirectory()) {
                    if (entry.getName().contains(".xlsx")) {
                        var excelImporter = new ExcelImporter();
                        var importDefinitionExcel = excelImporter.importExcel(zipInputStream);

                        importDefinition.setLocations(importDefinitionExcel.getLocations());
                        importDefinition.setDevices(importDefinitionExcel.getDevices());
                    } else

                    if (entry.getName().contains("aas/")) {
                        var pathSegments = entry.getName().split("/");
                        if (pathSegments.length == 3) {
                            var resourceIdSegement = pathSegments[1];
                            var aasxFileName = pathSegments[2];

                            UUID resourceId;
                            if (resourceIdSegement.contains("_")) {
                                var resourceIdSegments = resourceIdSegement.split("_");
                                resourceId = UUID.fromString(resourceIdSegments[resourceIdSegments.length-1]);
                            } else {
                                resourceId = UUID.fromString(resourceIdSegement);
                            }

                            InputStream entryInputStream = getInputStreamForCurrentEntry(zipInputStream);

                            aasxFilesOfResources
                                    .computeIfAbsent(resourceId, k -> new ArrayList<>())
                                    .add(entryInputStream);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }

            importDefinition.setAasxFiles(aasxFilesOfResources);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return importDefinition;
    }

    private InputStream getInputStreamForCurrentEntry(ZipInputStream zipInputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = zipInputStream.read(buffer)) > 0) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

}