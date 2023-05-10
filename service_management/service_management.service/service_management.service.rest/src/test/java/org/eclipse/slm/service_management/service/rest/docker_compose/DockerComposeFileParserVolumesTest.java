package org.eclipse.slm.service_management.service.rest.docker_compose;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.eclipse.slm.service_management.model.offerings.options.ServiceOptionValue;
import org.eclipse.slm.service_management.service.rest.docker_compose.*;
import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DockerComposeFileParser - Section 'services/{service}/volumes'")
public class DockerComposeFileParserVolumesTest {

    @Nested
    @DisplayName("Parsing")
    public class Parsing {

        @Test
        @DisplayName("Parse volumes with short syntax")
        public void parseVolumesWithShortSyntax() throws JsonProcessingException {
            var composeFile = """
                    version: "3"
                                        
                    services:
                      test-service:
                        image: "test-image:1.0.0"
                        volumes:
                          - /opt/data:/var/lib/mysql
                          - /var/lib/mysql
                          - ~/configs:/etc/configs/:ro
                          - datavolume:/var/lib/mysql
                    """;
            var bindMountAbsolutePath = new DockerComposeFileVolume(DockerComposeFileVolumeType.BIND);
            bindMountAbsolutePath.setSource("/opt/data");
            bindMountAbsolutePath.setTarget("/var/lib/mysql");
            var relativeUserPathReadonly = new DockerComposeFileVolume(DockerComposeFileVolumeType.BIND);
            relativeUserPathReadonly.setSource("~/configs");
            relativeUserPathReadonly.setTarget("/etc/configs/");
            relativeUserPathReadonly.setReadonly(true);
            var namedVolume = new DockerComposeFileVolume(DockerComposeFileVolumeType.VOLUME);
            namedVolume.setSource("datavolume");
            namedVolume.setTarget("/var/lib/mysql");
            var unnamedVolume = new DockerComposeFileVolume(DockerComposeFileVolumeType.VOLUME);
            unnamedVolume.setTarget("/var/lib/mysql");

            var parsedComposeFile = DockerComposeFileParser.parseComposeFile(composeFile);
            var parsedVolumes = parsedComposeFile.getServices().get("test-service").getVolumes();

            RecursiveComparisonConfiguration configuration = RecursiveComparisonConfiguration.builder().build();
            assertThat(parsedVolumes).usingRecursiveFieldByFieldElementComparator(configuration)
                    .contains(bindMountAbsolutePath)
                    .contains(relativeUserPathReadonly)
                    .contains(namedVolume)
                    .contains(unnamedVolume);
        }

        @Test
        @DisplayName("Parse volumes with long syntax")
        public void parseVolumesWithLongSyntax() throws JsonProcessingException {
            var composeFile = """
                    version: "3"
                                        
                    services:
                      test-service:
                        image: "test-image:1.0.0"
                        volumes:
                          - type: bind
                            source: /opt/data
                            target: /var/lib/mysql
                          - type: volume
                            source: mydata
                            target: /data
                            volume:
                              nocopy: true
                          - type: bind
                            source: ./static
                            target: /opt/app/static
                            bind:
                              propagation: "asdasd"
                          - type: tmpfs
                            target: /tmp
                            tmpfs:
                              size: 1000000
                    """;
            var bindMountAbsolutePath = new DockerComposeFileVolume(DockerComposeFileVolumeType.BIND);
            bindMountAbsolutePath.setSource("/opt/data");
            bindMountAbsolutePath.setTarget("/var/lib/mysql");
            var namedVolumeWithAdditionalOptions = new DockerComposeFileVolume(DockerComposeFileVolumeType.VOLUME);
            namedVolumeWithAdditionalOptions.setSource("mydata");
            namedVolumeWithAdditionalOptions.setTarget("/data");
            namedVolumeWithAdditionalOptions.setVolume(new DockerComposeFileVolumeOptions(true));
            var bindMountRelativePathWithAdditionalOptions = new DockerComposeFileVolume(DockerComposeFileVolumeType.BIND);
            bindMountRelativePathWithAdditionalOptions.setSource("./static");
            bindMountRelativePathWithAdditionalOptions.setTarget("/opt/app/static");
            bindMountRelativePathWithAdditionalOptions.setBind(new DockerComposeFileVolumeBindOptions("asdasd"));
            var tmpfsWithAdditionalOptions = new DockerComposeFileVolume(DockerComposeFileVolumeType.TMPFS);
            tmpfsWithAdditionalOptions.setTarget("/tmp");
            tmpfsWithAdditionalOptions.setTmpfs(new DockerComposeFileVolumeTmpfsOptions(1000000));

            var parsedComposeFile = DockerComposeFileParser.parseComposeFile(composeFile);
            var parsedVolumes = parsedComposeFile.getServices().get("test-service").getVolumes();

            RecursiveComparisonConfiguration configuration = RecursiveComparisonConfiguration.builder().build();
            assertThat(parsedVolumes).usingRecursiveFieldByFieldElementComparator(configuration)
                    .contains(bindMountAbsolutePath)
                    .contains(namedVolumeWithAdditionalOptions)
                    .contains(bindMountRelativePathWithAdditionalOptions)
                    .contains(tmpfsWithAdditionalOptions);
        }
    }

    @Nested
    @DisplayName("Service Options")
    public class ServiceOptions {
        @Test
        @DisplayName("Replace service option values for volumes")
        public void replaceServiceOptionValuesForVolumes() throws JSONException, JsonProcessingException {
            //region Expected Results
            var expectedComposeFile = """
                        version: '3'
                            
                        services:
                        
                          service1:
                            volumes:
                              - /my/custom/path/from/option:/var/lib/data
                              - my_custom_volume:/var/lib/config
                              
                          service2:
                            volumes:
                              - my_custom_volume2:/var/lib/config
                              - /service/config:/etc/service/config
                    """;
            //endregion

            //region Test Input
            var incomingComposeFile = DockerComposeFileParser.parseComposeFile("""
                        version: '3'
                            
                        services:
                        
                          service1:
                            image: testImage:latest
                            volumes:
                              - /my/path/volume:/var/lib/data
                              - my_named_volume:/var/lib/config
                              
                          service2:
                            image: testImage2:latest
                            volumes:
                              - my_service2_volume:/var/lib/config
                              - /service/config:/etc/service/config
                    """);

            var serviceOptionValues = List.of(
                    new ServiceOptionValue("service1|/var/lib/data", "/my/custom/path/from/option"),
                    new ServiceOptionValue("service1|/var/lib/config", "my_custom_volume"),
                    new ServiceOptionValue("service2|/var/lib/config", "my_custom_volume2"));
            //endregion

            var resultComposeFile = DockerComposeFileParser.replaceServiceOptionValuesForVolumes(serviceOptionValues, incomingComposeFile);
            DockerComposeFileParserTestUtil.assertComposeFiles(expectedComposeFile, resultComposeFile);
        }

    }
}
