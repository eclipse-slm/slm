package org.eclipse.slm.service_management.service.initializer;

import org.eclipse.slm.common.utils.files.FilesUtil;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.slm.service_management.service.initializer.GitRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class GitRepoInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    @Value("#{'${service-management.git-repos.clone-directory}'}")
    private String gitRepoCloneDirectory;

    @Value("#{'${service-management.git-repos.urls}'.split(',')}")
    private String[] gitRepoUrls;

    public List<String> cloneGitRepos() {
        var gitCloneDirectoryPath = this.gitRepoCloneDirectory;
        if (!this.gitRepoCloneDirectory.startsWith("/")) {
            gitCloneDirectoryPath = FilesUtil.getExecutionPath(this) + this.gitRepoCloneDirectory;
        }
        if (!gitCloneDirectoryPath.endsWith("/")) {
            gitCloneDirectoryPath += "/";
        }

        var clonedGitRepoDirectories = new ArrayList<String>();
        for (var gitRepoUrl : this.gitRepoUrls) {
            try {
                if (!gitRepoUrl.isEmpty()) {
                    var gitRepo = new GitRepo(gitRepoUrl);
                    LOG.info("Start cloning repository '" + gitRepo.getUrl() + "'");

                    var clonedGitRepoPath = gitCloneDirectoryPath + gitRepo.getName();
                    File clonedGitRepoDirectory = new File(clonedGitRepoPath);
                    if (clonedGitRepoDirectory.exists()) {
                        FileUtils.cleanDirectory(clonedGitRepoDirectory);
                    }
                    clonedGitRepoDirectories.add(String.valueOf(clonedGitRepoDirectory));

                    var gitCloneCommand = Git.cloneRepository()
                            .setURI(gitRepo.getUrl())
                            .setDirectory(clonedGitRepoDirectory);
                    if (gitRepo.getGitUsername() != null) {
                        UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
                                gitRepo.getGitUsername(), gitRepo.getGitPassword());
                        gitCloneCommand.setCredentialsProvider(credentialsProvider);
                    }

                    var git = gitCloneCommand.call();
                    git.checkout().setName(gitRepo.getBranchOrTag()).call();
                    LOG.info("Finished cloning repository '" + gitRepo.getUrl() + "'");
                }
            } catch (Exception e) {
                LOG.error(e.getMessage());
            }
        }

        return clonedGitRepoDirectories;
    }
}
