package org.eclipse.slm.common.utils.files;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileDownloader {

    public static MultipartFile downloadFile(String fileURL) throws Exception {
        URL url = new URL(fileURL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("HEAD");
        conn.connect();
        long size = conn.getContentLengthLong();
        String disposition = conn.getHeaderField("Content-Disposition");
        String contentType = conn.getContentType();
        String filename = null;
        if (disposition != null && disposition.contains("filename=")) {
            filename = disposition.split("filename=")[1].replace("\"", "").trim();
        } else {
            // Fallback: Extract filename from URL if header 'Content-Disposition' is not present
            String path = url.getPath();
            filename = path.substring(path.lastIndexOf('/') + 1);
        }
        conn.disconnect();

        try (InputStream httpInputStream = new URL(fileURL).openStream();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = httpInputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            var byteArrayInputStream = new ByteArrayInputStream(buffer.toByteArray());

            var multipartFile = new InputStreamMultipartFile(filename, byteArrayInputStream, contentType, size);
            return multipartFile;
        }
    }

}
