package org.eclipse.slm.service_management.service.initializer;

import org.eclipse.slm.service_management.service.initializer.GitRepo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GitRepoTests {

    @Nested
    @DisplayName("Parse Repo URL")
    public class parseRepoUrl {
        @Test
        @DisplayName("... without credentials, without branch or tag")
        public void parseRepoUrlWithoutCredentialsAndWithoutBranchOrTag() {
            var repoUrl = "https://github.com/FabOS-AI/fabos-slm-service-management-content.git";
            var expectedGitRepo = new GitRepo();
            expectedGitRepo.setUrl("https://github.com/FabOS-AI/fabos-slm-service-management-content.git");
            expectedGitRepo.setName("fabos-slm-service-management-content");

            var gitRepo = new GitRepo(repoUrl);

            assertThat(gitRepo).usingRecursiveComparison().isEqualTo(expectedGitRepo);
        }

        @Test
        @DisplayName("... with credentials, without branch or tag")
        public void parseRepoUrlWithCredentialsAndWithoutBranchOrTag() {
            var repoUrl = "myuser:mypass@https://github.com/FabOS-AI/fabos-slm-service-management-content.git";
            var expectedGitRepo = new GitRepo();
            expectedGitRepo.setUrl("https://github.com/FabOS-AI/fabos-slm-service-management-content.git");
            expectedGitRepo.setName("fabos-slm-service-management-content");
            expectedGitRepo.setGitUsername("myuser");
            expectedGitRepo.setGitPassword("mypass");

            var gitRepo = new GitRepo(repoUrl);

            assertThat(gitRepo).usingRecursiveComparison().isEqualTo(expectedGitRepo);
        }

        @Test
        @DisplayName("... without credentials, with branch or tag")
        public void parseRepoUrlWithoutCredentialsAndWithBranchOrTag() {
            var repoUrl = "https://github.com/FabOS-AI/fabos-slm-service-management-content.git:1.0.0";
            var expectedGitRepo = new GitRepo();
            expectedGitRepo.setUrl("https://github.com/FabOS-AI/fabos-slm-service-management-content.git");
            expectedGitRepo.setName("fabos-slm-service-management-content");
            expectedGitRepo.setBranchOrTag("1.0.0");

            var gitRepo = new GitRepo(repoUrl);

            assertThat(gitRepo).usingRecursiveComparison().isEqualTo(expectedGitRepo);
        }

        @Test
        @DisplayName("... with credentials, with branch or tag")
        public void parseRepoUrlWithCredentialsAndWithBranchOrTag() {
            var repoUrl = "myuser:mypass@https://github.com/FabOS-AI/fabos-slm-service-management-content.git:1.0.0";
            var expectedGitRepo = new GitRepo();
            expectedGitRepo.setUrl("https://github.com/FabOS-AI/fabos-slm-service-management-content.git");
            expectedGitRepo.setName("fabos-slm-service-management-content");
            expectedGitRepo.setGitUsername("myuser");
            expectedGitRepo.setGitPassword("mypass");
            expectedGitRepo.setBranchOrTag("1.0.0");

            var gitRepo = new GitRepo(repoUrl);

            assertThat(gitRepo).usingRecursiveComparison().isEqualTo(expectedGitRepo);
        }
    }
}
