package org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferings;


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

