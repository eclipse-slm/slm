package org.eclipse.slm.common.utils.files;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

public class FilesUtil {

    private static final Logger LOG = LoggerFactory.getLogger(FilesUtil.class);

    private static final String FILE_PATH_PREFIX = "file:/";

    private FilesUtil(){}

    public static <T> T loadFromFile(File file, TypeReference<?> type) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            ObjectReader reader = mapper.readerFor(type);
            return reader.readValue(file);
        } catch (JsonProcessingException e) {
            LOG.error("Unable to deserialize: {} to {}", file.getPath(), type, e);
        } catch (IOException e) {
            LOG.error("Unable to read file: {} to {}", file.getPath(), type, e);
        }
        return null;
    }

    public static <T> T loadFromFile(File file, Class<T> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            ObjectReader reader = mapper.readerFor(clazz);
            return reader.readValue(file);
        } catch (JsonProcessingException e) {
            LOG.error("Unable to deserialize: {} to {}", file.getPath(), clazz, e);
        } catch (IOException e) {
            LOG.error("Unable to read file: {} to {}", file.getPath(), clazz, e);
        }
        return null;
    }

    public static File getFile(String filePath) {
        LOG.debug("Get config file '{}'", filePath);
        try {
            Path path;
            if(filePath.startsWith(FILE_PATH_PREFIX)) {
                path =  Paths.get(URI.create(filePath));
            } else {
                path = Paths.get(filePath);
            }
            if(Files.exists(path)){
                return path.toFile();
            } else {
                LOG.warn("Unable to get file '{}'. Empty file has been created.", filePath);
                return Files.createFile(path).toFile();
            }
        } catch (NoSuchElementException | IOException e) {
            LOG.error("Can't read file '{}'", filePath, e);
            throw new RuntimeException(e);
        }
    }

    public static File[] findFiles(String searchDirectory, String filename, String fileExtension) {
        File searchFile = new File(searchDirectory);
        var files = searchFile.listFiles((dir, name) -> name.startsWith(filename) && name.endsWith(fileExtension));

        if (files == null) {
            files = new File[0];
        }

        return files;
    }

    public static String getExecutionPath(Object obj){
        String s = obj.getClass().getResource("/").getPath();
        if(!s.startsWith(FILE_PATH_PREFIX)) {
            //s= FILE_PATH_PREFIX+"/"+s;
            try {
                URI uri = new URI(s);
                return uri.toString();
            } catch (URISyntaxException e) {
                LOG.error("URISyntaxException during get execution path of '{}'", obj, e);
            }
        }
        return s;
    }

    public static byte[] loadFileBytes(String path) throws IOException {
        var filePath = pathOf(path);
        return Files.readAllBytes(filePath);
    }

    public static Path pathOf(String pathString) {
        if (pathString.startsWith("/") && pathString.contains(":"))
        {
            pathString = pathString.substring(1);
        }
        else if (pathString.startsWith("file:/"))
        {
            pathString = pathString.replace("file:/", "");
        }

        return Path.of(pathString);
    }

    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

}
