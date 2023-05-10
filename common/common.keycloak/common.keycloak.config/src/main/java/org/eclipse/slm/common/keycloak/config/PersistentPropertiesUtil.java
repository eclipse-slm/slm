package org.eclipse.slm.common.keycloak.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The type Persistent properties util.
 *
 * @author des
 */
public class PersistentPropertiesUtil {

    private static final Logger LOG = LoggerFactory.getLogger(PersistentPropertiesUtil.class);

    private static final String FILE_PATH_PREFIX = "file:/";

    private PersistentPropertiesUtil(){}

    /**
     * Save to file.
     *
     * @param file the file
     * @param obj  the obj
     */
    public static void saveToFile(File file, Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            String s = writer.writeValueAsString(obj);
            FileUtils.writeStringToFile(file, s, StandardCharsets.UTF_8.name(), false);
        } catch (JsonProcessingException e) {
            LOG.error("Unable to serialize: {}", file.getPath(), e);
        } catch (IOException e) {
            LOG.error("Unable to write file: {}", file.getPath(), e);
        }
    }

    /**
     * Load from file.
     *
     * @param <T>   the type parameter
     * @param file  the file
     * @param clazz the clazz
     * @return the t
     */
    public static <T> T loadFromFile(File file, Class<T> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ObjectReader reader = mapper.readerFor(clazz);
            return reader.readValue(file);
        } catch (JsonProcessingException e) {
            LOG.error("Unable to deserialize: {} to {}", file.getPath(), clazz, e);
        } catch (IOException e) {
            LOG.error("Unable to read file: {} to {}", file.getPath(), clazz, e);
        }
        return null;
    }

    /**
     * Gets file.
     *
     * @param filePath       the file path
     * @return the file
     */
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

    /**
     * Find files in directory by PathMatcher pattern.
     *
     * @param searchDirectory the search directory
     * @param pattern         the pattern
     * @return the map
     */
    public static Map<String, File> findFiles(String searchDirectory, String pattern) {
        LOG.info("Find files '{}' in '{}'", searchDirectory, pattern);
        try {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
            Path path;
            if(searchDirectory.startsWith(FILE_PATH_PREFIX)) {
                path =  Paths.get(URI.create(searchDirectory));
            } else {
                path = Paths.get(searchDirectory);
            }
            try (Stream<Path> files = Files.walk(path)) {
                return files
                        .filter(matcher::matches)
                        .collect(Collectors.toMap(p -> p.getFileName().toString() ,p -> p.toFile()));
            }
        } catch (IOException e) {
            LOG.error("Can't read files '{}' in '{}'", searchDirectory, pattern);
            return new HashMap<>();
        }
    }

    /**
     * Create file and directory if not exists.
     *
     * @param sourceDirectory the source directory
     * @param filePath        the file path
     * @return the file
     */
    public static File createFile(String sourceDirectory, String filePath) throws FileAlreadyExistsException {
        try {
            if(!filePath.startsWith("/")) {
                filePath = "/" + filePath;
            }
            Path path;
            if(sourceDirectory.startsWith(FILE_PATH_PREFIX)) {
                path =  Paths.get(URI.create(sourceDirectory + filePath));
            } else {
                path = Paths.get(sourceDirectory + filePath);
            }
            File f = path.toFile();
            if (!f.exists()) {
                // create directories in path if not exists
                f.getParentFile().mkdirs();
                //creat the file
                f.createNewFile();
                return f;
            } else {
                throw new FileAlreadyExistsException("File " + path + " exists");
            }
        } catch (FileAlreadyExistsException e) {
            throw e;
        } catch (IOException e) {
            LOG.error("Can't create file '{}' in '{}'", filePath, sourceDirectory, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Get execution path string.
     *
     * @param obj the obj
     * @return the string
     */
    public static String getExecutionPath(Object obj){
        String s = obj.getClass().getResource("/").getPath();
        if(!s.startsWith(FILE_PATH_PREFIX)) {
            s= FILE_PATH_PREFIX+"/"+s;
            try {
                URI uri = new URI(s);
                return uri.toString();
            } catch (URISyntaxException e) {
                LOG.error("URISyntaxException during get execution path of '{}'", obj, e);
            }
        }
        return s;
    }

}
