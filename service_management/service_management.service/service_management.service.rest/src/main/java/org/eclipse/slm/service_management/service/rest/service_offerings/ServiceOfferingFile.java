package org.eclipse.slm.service_management.service.rest.service_offerings;

import java.io.InputStream;

public class ServiceOfferingFile {
    private final String fileName;
    private final InputStream fileStream;

    public ServiceOfferingFile(String fileName, InputStream fileStream) {
        this.fileName = fileName;
        this.fileStream = fileStream;
    }

    public String getFileName() {
        return fileName;
    }

    public InputStream getFileStream() {
        return fileStream;
    }
}
