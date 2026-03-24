package org.eclipse.slm.common.utils.files;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamMultipartFile implements MultipartFile {

    private final String filename;

    private final InputStream inputStream;

    private final String contentType;

    private final long size;

    public InputStreamMultipartFile(String filename, InputStream inputStream, String contentType, long size) {
        this.filename = filename;
        this.inputStream = inputStream;
        this.contentType = contentType;
        this.size = size;
    }

    @Override
    public String getName() {
        return this.getOriginalFilename();
    }

    @Override
    public String getOriginalFilename() {
        return this.filename;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return inputStream.readAllBytes();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.inputStream;
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        transferTo(dest.toPath());
    }
}
