package org.eclipse.slm.service_management.service.rest.service_offerings;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ServiceOfferingGitUpdateTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceOfferingGitUpdateTask.class);

    private List<String> knownTags;

    private Git gitRepository;

    private CredentialsProvider credentialsProvider;

    private int updateIntervalMinutes;

    private ServiceOfferingGitUpdateTaskListener listener;

    private ScheduledExecutorService scheduledExecutorService;

    public ServiceOfferingGitUpdateTask(Git gitRepository, CredentialsProvider credentialsProvider,
                                        int updateIntervalMinutes, ServiceOfferingGitUpdateTaskListener listener) {
        this.gitRepository = gitRepository;
        this.credentialsProvider = credentialsProvider;
        this.updateIntervalMinutes = updateIntervalMinutes;
        this.listener = listener;

        this.knownTags = this.getTags();

        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        this.scheduledExecutorService.scheduleAtFixedRate(this, 0, this.updateIntervalMinutes, TimeUnit.MINUTES);
    }

    public void stop() {
        this.scheduledExecutorService.shutdown();
    }

    @Override
    public void run() {
        try {
            var allTags = this.getTags();
            var newTags = new ArrayList<String>(allTags);
            var removedTags = new ArrayList<String>();
            for (var knownTag : knownTags) {
                newTags.remove(knownTag);
                if (!allTags.contains(knownTag)) {
                    removedTags.add(knownTag);
                }
            }
            if (newTags.size() > 0) {
                this.knownTags = allTags;
                this.listener.onNewTagsDetected(this, newTags);
            }

            if (removedTags.size() > 0) {
                this.knownTags = allTags;
                this.listener.onTagsDeleted(this, removedTags);
            }
        } catch (Exception e) {
            LOG.error("Error during git tag update: " + e.getMessage());
        }
    }

    private List<String> getTags() {
        List<String> tags;
        try {
            var remoteTagRefs = this.gitRepository.lsRemote()
                    .setRemote("origin")
                    .setCredentialsProvider(credentialsProvider)
                    .setHeads(false)
                    .setTags(true)
                    .call();
            tags = remoteTagRefs.stream().map(tagRef -> tagRef.getName().replace("refs/tags/", "")).collect(Collectors.toList());
        } catch (GitAPIException e) {
            LOG.error("Unable to get tags of git repo '" + this.gitRepository.getRepository().getWorkTree().getAbsolutePath() + "': " + e);
            tags = new ArrayList<>();
        }

        return tags;
    }

}
