package org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferings;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServiceOfferingGitUpdaterTest {

    private static final Date COMMIT_DATE = new Date(1_700_000_000_000L);
    private static final Date TAGGER_DATE = new Date(1_710_000_000_000L);

    @Test
    public void getTagDate_lightweightTag_returnsCommitDate(@TempDir Path repoDir) throws Exception {
        try (var git = Git.init().setDirectory(repoDir.toFile()).call()) {
            var commitIdent = new PersonIdent("test", "test@example.com", COMMIT_DATE, TimeZone.getDefault());
            git.commit().setAllowEmpty(true).setMessage("init").setAuthor(commitIdent).setCommitter(commitIdent).call();
            var tagRef = git.tag().setName("1.0.0").setAnnotated(false).call();

            var tagDate = ServiceOfferingGitUpdater.getTagDate(git.getRepository(), tagRef);

            assertEquals(COMMIT_DATE, tagDate);
        }
    }

    @Test
    public void getTagDate_annotatedTag_returnsTaggerDate(@TempDir Path repoDir) throws Exception {
        try (var git = Git.init().setDirectory(repoDir.toFile()).call()) {
            var commitIdent = new PersonIdent("test", "test@example.com", COMMIT_DATE, TimeZone.getDefault());
            git.commit().setAllowEmpty(true).setMessage("init").setAuthor(commitIdent).setCommitter(commitIdent).call();
            var taggerIdent = new PersonIdent("test", "test@example.com", TAGGER_DATE, TimeZone.getDefault());
            var tagRef = git.tag().setName("1.0.0").setAnnotated(true).setMessage("1.0.0").setTagger(taggerIdent).call();

            var tagDate = ServiceOfferingGitUpdater.getTagDate(git.getRepository(), tagRef);

            assertEquals(TAGGER_DATE, tagDate);
        }
    }
}
