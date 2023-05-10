package org.eclipse.slm.service_management.service.rest.docker_compose;

import org.eclipse.slm.service_management.service.rest.docker_compose.DockerComposeFileVolume;
import org.eclipse.slm.service_management.service.rest.docker_compose.DockerComposeFileVolumeBindOptions;
import org.eclipse.slm.service_management.service.rest.docker_compose.DockerComposeFileVolumeOptions;
import org.eclipse.slm.service_management.service.rest.docker_compose.DockerComposeFileVolumeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class DockerComposeFileVolumeTest {

    @Nested
    @DisplayName("Parsing from short syntax")
    public class ParsingFromShortSyntax {

        @Test
        @DisplayName("Unnamed volume")
        public void unnamedVolume() {
            var volumeStringShortSyntax = "/var/lib/mysql";
            var expectedVolume = new DockerComposeFileVolume(DockerComposeFileVolumeType.VOLUME);
            expectedVolume.setTarget(volumeStringShortSyntax);

            var parsedVolume = new DockerComposeFileVolume(volumeStringShortSyntax);
            assertThat(parsedVolume).usingRecursiveComparison()
                    .isEqualTo(expectedVolume);
        }

        @Test
        @DisplayName("Named volume")
        public void namedVolume() {
            var volumeStringShortSyntax = "datavolume:/var/lib/mysql";
            var expectedVolume = new DockerComposeFileVolume(DockerComposeFileVolumeType.VOLUME);
            expectedVolume.setSource(volumeStringShortSyntax.split(":")[0]);
            expectedVolume.setTarget(volumeStringShortSyntax.split(":")[1]);

            var parsedVolume = new DockerComposeFileVolume(volumeStringShortSyntax);
            assertThat(parsedVolume).usingRecursiveComparison()
                    .isEqualTo(expectedVolume);
        }

        @Test
        @DisplayName("Named volume readonly")
        public void namedVolumeReadonly() {
            var volumeStringShortSyntax = "datavolume:/var/lib/mysql:ro";
            var expectedVolume = new DockerComposeFileVolume(DockerComposeFileVolumeType.VOLUME);
            expectedVolume.setSource(volumeStringShortSyntax.split(":")[0]);
            expectedVolume.setTarget(volumeStringShortSyntax.split(":")[1]);
            expectedVolume.setReadonly(true);

            var parsedVolume = new DockerComposeFileVolume(volumeStringShortSyntax);
            assertThat(parsedVolume).usingRecursiveComparison()
                    .isEqualTo(expectedVolume);
        }

        @Test
        @DisplayName("Bind mount | Absolute path")
        public void bindMountAbsolutePath() {
            var volumeStringShortSyntax = "/opt/data:/var/lib/mysql";
            var expectedVolume = new DockerComposeFileVolume(DockerComposeFileVolumeType.BIND);
            expectedVolume.setSource(volumeStringShortSyntax.split(":")[0]);
            expectedVolume.setTarget(volumeStringShortSyntax.split(":")[1]);

            var parsedVolume = new DockerComposeFileVolume(volumeStringShortSyntax);
            assertThat(parsedVolume).usingRecursiveComparison()
                    .isEqualTo(expectedVolume);
        }

        @Test
        @DisplayName("Bind mount | Absolute path | Readonly")
        public void bindMountAbsolutePathReadonly() {
            var volumeStringShortSyntax = "/opt/data:/var/lib/mysql:ro";
            var expectedVolume = new DockerComposeFileVolume(DockerComposeFileVolumeType.BIND);
            expectedVolume.setSource(volumeStringShortSyntax.split(":")[0]);
            expectedVolume.setTarget(volumeStringShortSyntax.split(":")[1]);
            expectedVolume.setReadonly(true);

            var parsedVolume = new DockerComposeFileVolume(volumeStringShortSyntax);
            assertThat(parsedVolume).usingRecursiveComparison()
                    .isEqualTo(expectedVolume);
        }

        @Test
        @DisplayName("Bind mount | Relative path")
        public void bindMountRelativePath() {
            var volumeStringShortSyntax = "./cache:/tmp/cache";
            var expectedVolume = new DockerComposeFileVolume(DockerComposeFileVolumeType.BIND);
            expectedVolume.setSource(volumeStringShortSyntax.split(":")[0]);
            expectedVolume.setTarget(volumeStringShortSyntax.split(":")[1]);

            var parsedVolume = new DockerComposeFileVolume(volumeStringShortSyntax);
            assertThat(parsedVolume).usingRecursiveComparison()
                    .isEqualTo(expectedVolume);
        }

        @Test
        @DisplayName("Bind mount | User-relative path")
        public void bindMountRelativPath() {
            var volumeStringShortSyntax = "~/configs:/etc/configs/";
            var expectedVolume = new DockerComposeFileVolume(DockerComposeFileVolumeType.BIND);
            expectedVolume.setSource(volumeStringShortSyntax.split(":")[0]);
            expectedVolume.setTarget(volumeStringShortSyntax.split(":")[1]);

            var parsedVolume = new DockerComposeFileVolume(volumeStringShortSyntax);
            assertThat(parsedVolume).usingRecursiveComparison()
                    .isEqualTo(expectedVolume);
        }

    }

    @Nested
    @DisplayName("Converting to short syntax")
    public class ConvertingToShortSyntax {

        @Test
        @DisplayName("Unnamed volume")
        public void unnamedVolume() {
            var expectedVolumeStringShortSyntax = "/var/lib/mysql";
            var volume = new DockerComposeFileVolume(DockerComposeFileVolumeType.VOLUME);
            volume.setTarget(expectedVolumeStringShortSyntax);

            var generatedVolumeString = volume.toVolumeString();
            assertThat(generatedVolumeString).isEqualTo(expectedVolumeStringShortSyntax);
        }

        @Test
        @DisplayName("Named volume")
        public void namedVolume() {
            var expectedVolumeStringShortSyntax = "datavolume:/var/lib/mysql";
            var volume = new DockerComposeFileVolume(DockerComposeFileVolumeType.VOLUME);
            volume.setSource(expectedVolumeStringShortSyntax.split(":")[0]);
            volume.setTarget(expectedVolumeStringShortSyntax.split(":")[1]);

            var generatedVolumeString = volume.toVolumeString();
            assertThat(generatedVolumeString).isEqualTo(expectedVolumeStringShortSyntax);
        }

        @Test
        @DisplayName("Named volume readonly")
        public void namedVolumeReadonly() {
            var expectedVolumeStringShortSyntax = "datavolume:/var/lib/mysql:ro";
            var volume = new DockerComposeFileVolume(DockerComposeFileVolumeType.VOLUME);
            volume.setSource(expectedVolumeStringShortSyntax.split(":")[0]);
            volume.setTarget(expectedVolumeStringShortSyntax.split(":")[1]);
            volume.setReadonly(true);

            var generatedVolumeString = volume.toVolumeString();
            assertThat(generatedVolumeString).isEqualTo(expectedVolumeStringShortSyntax);
        }

        @Test
        @DisplayName("Volume with additional options")
        public void volumeWithAdditionalOptions() {
            var volume = new DockerComposeFileVolume(DockerComposeFileVolumeType.VOLUME);
            volume.setVolume(new DockerComposeFileVolumeOptions(false));

            var generatedVolumeString = volume.toVolumeString();
            assertThat(generatedVolumeString).contains("Short syntax is not available");
        }

        @Test
        @DisplayName("Bind mount | Absolute path")
        public void bindMountAbsolutePath() {
            var expectedVolumeStringShortSyntax = "/opt/data:/var/lib/mysql";
            var volume = new DockerComposeFileVolume(DockerComposeFileVolumeType.BIND);
            volume.setSource(expectedVolumeStringShortSyntax.split(":")[0]);
            volume.setTarget(expectedVolumeStringShortSyntax.split(":")[1]);

            var generatedVolumeString = volume.toVolumeString();
            assertThat(generatedVolumeString).isEqualTo(expectedVolumeStringShortSyntax);
        }

        @Test
        @DisplayName("Bind mount | Absolute path | Readonly")
        public void bindMountAbsolutePathReadonly() {
            var expectedVolumeStringShortSyntax = "/opt/data:/var/lib/mysql:ro";
            var volume = new DockerComposeFileVolume(DockerComposeFileVolumeType.BIND);
            volume.setSource(expectedVolumeStringShortSyntax.split(":")[0]);
            volume.setTarget(expectedVolumeStringShortSyntax.split(":")[1]);
            volume.setReadonly(true);

            var generatedVolumeString = volume.toVolumeString();
            assertThat(generatedVolumeString).isEqualTo(expectedVolumeStringShortSyntax);
        }

        @Test
        @DisplayName("Bind mount | Relative path")
        public void bindMountRelativePath() {
            var expectedVolumeStringShortSyntax = "./cache:/tmp/cache";
            var volume = new DockerComposeFileVolume(DockerComposeFileVolumeType.BIND);
            volume.setSource(expectedVolumeStringShortSyntax.split(":")[0]);
            volume.setTarget(expectedVolumeStringShortSyntax.split(":")[1]);

            var generatedVolumeString = volume.toVolumeString();
            assertThat(generatedVolumeString).isEqualTo(expectedVolumeStringShortSyntax);
        }

        @Test
        @DisplayName("Bind mount | User-relative path")
        public void bindMountRelativPath() {
            var expectedVolumeStringShortSyntax = "~/configs:/etc/configs/";
            var volume = new DockerComposeFileVolume(DockerComposeFileVolumeType.BIND);
            volume.setSource(expectedVolumeStringShortSyntax.split(":")[0]);
            volume.setTarget(expectedVolumeStringShortSyntax.split(":")[1]);

            var generatedVolumeString = volume.toVolumeString();
            assertThat(generatedVolumeString).isEqualTo(expectedVolumeStringShortSyntax);
        }

        @Test
        @DisplayName("Bind mount with additional options")
        public void bindMountWithAdditionalOptions() {
            var volume = new DockerComposeFileVolume(DockerComposeFileVolumeType.BIND);
            volume.setBind(new DockerComposeFileVolumeBindOptions("asdasdasd"));

            var generatedVolumeString = volume.toVolumeString();
            assertThat(generatedVolumeString).contains("Short syntax is not available");
        }

        @Test
        @DisplayName("tmpfs")
        public void tmpfs() {
            var volume = new DockerComposeFileVolume(DockerComposeFileVolumeType.TMPFS);

            var generatedVolumeString = volume.toVolumeString();
            assertThat(generatedVolumeString).contains("Short syntax is not available");
        }

        @Test
        @DisplayName("npipe")
        public void npipe() {
            var volume = new DockerComposeFileVolume(DockerComposeFileVolumeType.TMPFS);

            var generatedVolumeString = volume.toVolumeString();
            assertThat(generatedVolumeString).contains("Short syntax is not available");
        }
    }
}
