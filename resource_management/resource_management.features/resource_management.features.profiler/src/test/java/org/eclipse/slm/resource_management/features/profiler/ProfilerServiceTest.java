package org.eclipse.slm.resource_management.features.profiler;

import org.eclipse.slm.awx.client.AwxClient;
import org.eclipse.slm.awx.client.AwxProjectUpdateFailedException;
import org.eclipse.slm.awx.client.observer.AwxJobExecutor;
import org.eclipse.slm.awx.model.JobTemplate;
import org.eclipse.slm.awx.model.Project;
import org.eclipse.slm.awx.model.Results;
import org.eclipse.slm.awx.model.SurveyItem;
import org.eclipse.slm.awx.model.SurveyItemType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ProfilerServiceTest {

    private static final int OLD_PROJECT_ID = 11;
    private static final int OLD_TEMPLATE_ID = 21;

    private ProfilerJpaRepository repository;
    private AwxClient awxClient;
    private ProfilerService service;

    private Profiler stored;

    @BeforeEach
    void setUp() {
        repository = mock(ProfilerJpaRepository.class);
        awxClient = mock(AwxClient.class);
        service = new ProfilerService(repository, awxClient, mock(AwxJobExecutor.class));

        stored = profiler("old", action("repo", "main", "play.yml", null));
        when(repository.findById(stored.getId())).thenReturn(java.util.Optional.of(stored));
        when(repository.findAll()).thenReturn(List.of(stored));
        when(repository.save(any(Profiler.class))).thenAnswer(i -> i.getArgument(0));
        when(awxClient.getJobTemplateFromProjectId(anyInt())).thenReturn(results(Collections.emptyList()));
    }

    private static AwxAction action(String repo, String branch, String playbook, List<SurveyItem> parameter) {
        var action = new AwxAction(repo, branch, playbook);
        action.setParameter(parameter);
        return action;
    }

    private static Profiler profiler(String name, Action action) {
        var profiler = new Profiler();
        profiler.setName(name);
        profiler.setAction(action);
        return profiler;
    }

    private static SurveyItem item(String variable) {
        return new SurveyItem(SurveyItemType.text, "name", "desc", variable);
    }

    private static <T> Results<T> results(List<T> items) {
        return new Results<>(items.size(), null, null, items);
    }

    private static Project project(int id, String repo) {
        var project = mock(Project.class);
        when(project.getId()).thenReturn(id);
        when(project.getScm_url()).thenReturn(repo);
        when(project.getCredential()).thenReturn(null);
        return project;
    }

    private void stubTemplate(int projectId, String playbook, int templateId) {
        var template = mock(JobTemplate.class);
        when(template.getId()).thenReturn(templateId);
        when(awxClient.getJobTemplateFromProjectIdAndPlaybook(projectId, playbook)).thenReturn(results(List.of(template)));
    }

    @SafeVarargs
    private void stubProjects(String repo, String branch, List<Project>... consecutiveLookups) {
        var stubbing = when(awxClient.getProjectFromRepoAndBranch(repo, branch)).thenReturn(results(consecutiveLookups[0]));
        for (var i = 1; i < consecutiveLookups.length; i++) {
            stubbing = stubbing.thenReturn(results(consecutiveLookups[i]));
        }
    }

    private void stubExisting(String repo, String branch, String playbook, int projectId, int templateId) {
        stubProjects(repo, branch, List.of(project(projectId, repo)));
        stubTemplate(projectId, playbook, templateId);
    }

    private void verifyNothingRemoved() throws Throwable {
        verify(awxClient, never()).deleteJobTemplate(anyInt());
        verify(awxClient, never()).deleteProject(anyInt());
        verify(awxClient, never()).deleteJobTemplate(anyString(), anyString(), anyString());
        verify(awxClient, never()).deleteProject(anyString(), anyString());
    }

    private void stubAbsent(String repo, String branch) {
        when(awxClient.getProjectFromRepoAndBranch(repo, branch)).thenReturn(results(Collections.emptyList()));
    }

    private JobTemplate createdTemplate(int id) {
        var template = mock(JobTemplate.class);
        when(template.getId()).thenReturn(id);
        when(template.getName()).thenReturn("tpl");
        return template;
    }

    private void stubCreation() throws Throwable {
        var template = createdTemplate(31);
        when(awxClient.createJobTemplateAndAddExecuteRoleToDefaultTeam(any(), any(), any(), any(), any())).thenReturn(template);
    }

    @Test
    void renameOnlyDoesNotTouchAwx() {
        var update = profiler("new", action("repo", "main", "play.yml", null));

        service.updateProfiler(stored.getId(), update);

        verify(repository).save(argThat(p -> p.getName().equals("new") && p.getId().equals(stored.getId())));
        verifyNoInteractions(awxClient);
    }

    @Test
    void renameWithNullVersusEmptyParametersDoesNotTouchAwx() {
        var update = profiler("new", action("repo", "main", "play.yml", Collections.emptyList()));

        service.updateProfiler(stored.getId(), update);

        verify(repository).save(any(Profiler.class));
        verifyNoInteractions(awxClient);
    }

    @Test
    void parameterChangeReplacesSurveyOnly() throws Throwable {
        stubExisting("repo", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        var update = profiler("old", action("repo", "main", "play.yml", List.of(item("a"))));

        service.updateProfiler(stored.getId(), update);

        verify(awxClient).deleteSurvey(OLD_TEMPLATE_ID);
        verify(awxClient).createSurvey(eq(OLD_TEMPLATE_ID), any());
        verify(awxClient).enableSurvey(OLD_TEMPLATE_ID);
        verify(awxClient, never()).deleteJobTemplate(anyString(), anyString(), anyString());
        verify(awxClient, never()).deleteProject(anyString(), anyString());
        verify(awxClient, never()).createJobTemplateAndAddExecuteRoleToDefaultTeam(any(), any(), any(), any(), any());
        verify(awxClient, never()).createJobTemplateAddExecuteRoleToDefaultTeamAddScmCredential(any(), any(), any(), any(), any(), any(), any());
        verify(repository).save(any(Profiler.class));
    }

    @Test
    void changeOfSurveyItemFieldOutsideEqualsReplacesSurvey() throws Throwable {
        ((AwxAction) stored.getAction()).setParameter(List.of(item("a")));
        stubExisting("repo", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        var changedItem = item("a");
        changedItem.setRequired(true);
        var update = profiler("old", action("repo", "main", "play.yml", List.of(changedItem)));

        service.updateProfiler(stored.getId(), update);

        verify(awxClient).deleteSurvey(OLD_TEMPLATE_ID);
        verify(awxClient).createSurvey(eq(OLD_TEMPLATE_ID), any());
    }

    @Test
    void renameWithUnchangedParametersDoesNotTouchAwx() {
        ((AwxAction) stored.getAction()).setParameter(List.of(item("a")));
        var update = profiler("new", action("repo", "main", "play.yml", List.of(item("a"))));

        service.updateProfiler(stored.getId(), update);

        verifyNoInteractions(awxClient);
    }

    @Test
    void clearingParametersDeletesSurveyWithoutCreating() throws Throwable {
        ((AwxAction) stored.getAction()).setParameter(List.of(item("a")));
        stubExisting("repo", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        var update = profiler("old", action("repo", "main", "play.yml", Collections.emptyList()));

        service.updateProfiler(stored.getId(), update);

        verify(awxClient).deleteSurvey(OLD_TEMPLATE_ID);
        verify(awxClient, never()).createSurvey(anyInt(), any());
        verify(awxClient, never()).enableSurvey(anyInt());
        verify(repository).save(any(Profiler.class));
    }

    @Test
    void parameterChangeWithMissingTemplateFailsWithoutSaving() {
        stubAbsent("repo", "main");
        var update = profiler("old", action("repo", "main", "play.yml", List.of(item("a"))));

        assertThrows(ProfilerRuntimeException.class, () -> service.updateProfiler(stored.getId(), update));

        verify(repository, never()).save(any(Profiler.class));
    }

    @Test
    void sourceChangeCreatesNewAndSavesBeforeDeletingOld() throws Throwable {
        stubExisting("repo", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        stubAbsent("newRepo", "dev");
        stubCreation();
        var update = profiler("old", action("newRepo", "dev", "new.yml", null));

        var result = service.updateProfiler(stored.getId(), update);

        assertEquals(stored.getId(), result.getId());
        InOrder inOrder = inOrder(awxClient, repository);
        inOrder.verify(awxClient).createJobTemplateAndAddExecuteRoleToDefaultTeam(eq("newRepo"), eq("dev"), eq("new.yml"), anyList(), any());
        inOrder.verify(repository).save(argThat(p -> p.getId().equals(stored.getId())));
        inOrder.verify(awxClient).deleteJobTemplate(OLD_TEMPLATE_ID);
        inOrder.verify(awxClient).deleteProject(OLD_PROJECT_ID);
    }

    @Test
    void sourceChangeWithEmptyParametersCreatesNoSurvey() throws Throwable {
        stubAbsent("repo", "main");
        stubAbsent("newRepo", "dev");
        stubCreation();

        service.updateProfiler(stored.getId(), profiler("old", action("newRepo", "dev", "new.yml", Collections.emptyList())));

        verify(awxClient, never()).createSurvey(anyInt(), any());
        verify(awxClient, never()).enableSurvey(anyInt());
    }

    @Test
    void urlChangeResolvingToOldResourcesRemovesNothing() throws Throwable {
        ((AwxAction) stored.getAction()).setAwxRepo("https://host/x.git");
        stubExisting("https://host/x.git", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        // AWX matches the URL as a case-insensitive substring, so the shorter URL finds the old project
        stubProjects("https://host/x", "main", List.of(project(OLD_PROJECT_ID, "https://host/x.git")));
        stubCreation();

        service.updateProfiler(stored.getId(), profiler("old", action("https://host/x", "main", "play.yml", null)));

        verifyNothingRemoved();
        verify(repository).save(argThat(p -> ((AwxAction) p.getAction()).getAwxRepo().equals("https://host/x")));
    }

    @Test
    void caseOnlyUrlChangeRemovesNothing() throws Throwable {
        ((AwxAction) stored.getAction()).setAwxRepo("https://host/X");
        stubExisting("https://host/X", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        stubProjects("https://host/x", "main", List.of(project(OLD_PROJECT_ID, "https://host/X")));
        stubCreation();

        service.updateProfiler(stored.getId(), profiler("old", action("https://host/x", "main", "play.yml", null)));

        verifyNothingRemoved();
        verify(repository).save(any(Profiler.class));
    }

    @Test
    void urlChangeToLongerUrlRemovesOnlyOldResources() throws Throwable {
        ((AwxAction) stored.getAction()).setAwxRepo("https://host/x");
        var newProject = project(51, "https://host/x.git");
        var oldProject = project(OLD_PROJECT_ID, "https://host/x");
        // The old URL is a substring of the new one, so looking it up finds both projects, new one first
        stubProjects("https://host/x", "main", List.of(newProject, oldProject));
        stubProjects("https://host/x.git", "main", List.of(), List.of(newProject));
        stubTemplate(51, "play.yml", 61);
        stubTemplate(OLD_PROJECT_ID, "play.yml", OLD_TEMPLATE_ID);
        stubCreation();

        service.updateProfiler(stored.getId(), profiler("old", action("https://host/x.git", "main", "play.yml", null)));

        verify(awxClient).deleteJobTemplate(OLD_TEMPLATE_ID);
        verify(awxClient).deleteProject(OLD_PROJECT_ID);
        verify(awxClient, never()).deleteJobTemplate(61);
        verify(awxClient, never()).deleteProject(51);
    }

    @Test
    void playbookChangeRemovesOldTemplateButKeepsProject() throws Throwable {
        stubExisting("repo", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        stubCreation();

        service.updateProfiler(stored.getId(), profiler("old", action("repo", "main", "new.yml", null)));

        verify(awxClient).deleteJobTemplate(OLD_TEMPLATE_ID);
        verify(awxClient, never()).deleteProject(anyInt());
    }

    @Test
    void removedProjectAlsoRemovesItsUnusedCredential() throws Throwable {
        var oldProject = project(OLD_PROJECT_ID, "repo");
        when(oldProject.getCredential()).thenReturn(71);
        stubProjects("repo", "main", List.of(oldProject));
        stubTemplate(OLD_PROJECT_ID, "play.yml", OLD_TEMPLATE_ID);
        when(awxClient.getProjectFromCredential(71)).thenReturn(results(Collections.emptyList()));

        service.deleteProfiler(stored.getId());

        verify(awxClient).deleteProject(OLD_PROJECT_ID);
        verify(awxClient).deleteCredential(71);
    }

    @Test
    void projectWithOtherJobTemplatesIsKept() throws Throwable {
        stubExisting("repo", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        var otherTemplate = mock(JobTemplate.class);
        when(awxClient.getJobTemplateFromProjectId(OLD_PROJECT_ID)).thenReturn(results(List.of(otherTemplate)));

        service.deleteProfiler(stored.getId());

        verify(awxClient).deleteJobTemplate(OLD_TEMPLATE_ID);
        verify(awxClient, never()).deleteProject(anyInt());
    }

    @Test
    void failedSaveRemovesNewResourcesAndKeepsOld() throws Throwable {
        stubExisting("repo", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        stubProjects("newRepo", "dev", List.of(), List.of(project(51, "newRepo")));
        stubTemplate(51, "new.yml", 61);
        stubCreation();
        when(repository.save(any(Profiler.class))).thenThrow(new RuntimeException("database"));

        assertThrows(ProfilerRuntimeException.class,
                () -> service.updateProfiler(stored.getId(), profiler("old", action("newRepo", "dev", "new.yml", null))));

        verify(awxClient).deleteJobTemplate(61);
        verify(awxClient).deleteProject(51);
        verify(awxClient, never()).deleteJobTemplate(OLD_TEMPLATE_ID);
        verify(awxClient, never()).deleteProject(OLD_PROJECT_ID);
    }

    @Test
    void sourceChangeWithCredentialsUsesScmCredentialVariant() throws Throwable {
        stubAbsent("repo", "main");
        stubAbsent("newRepo", "dev");
        var template = createdTemplate(31);
        when(awxClient.createJobTemplateAddExecuteRoleToDefaultTeamAddScmCredential(
                eq("newRepo"), eq("dev"), eq("new.yml"), eq("user"), eq("pw"), anyList(), any()))
                .thenReturn(template);
        var newAction = action("newRepo", "dev", "new.yml", null);
        newAction.setUsername("user");
        newAction.setPassword("pw");

        service.updateProfiler(stored.getId(), profiler("old", newAction));

        verify(awxClient).createJobTemplateAddExecuteRoleToDefaultTeamAddScmCredential(
                eq("newRepo"), eq("dev"), eq("new.yml"), eq("user"), eq("pw"), anyList(), any());
        verify(awxClient, never()).createJobTemplateAndAddExecuteRoleToDefaultTeam(any(), any(), any(), any(), any());
    }

    @Test
    void failedCreationKeepsOldAndRemovesOnlyNewResources() throws Throwable {
        stubExisting("repo", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        stubProjects("newRepo", "dev", List.of(), List.of(project(51, "newRepo")));
        stubTemplate(51, "new.yml", 61);
        when(awxClient.createJobTemplateAndAddExecuteRoleToDefaultTeam(any(), any(), any(), any(), any()))
                .thenThrow(new AwxProjectUpdateFailedException("failed", null));
        var update = profiler("old", action("newRepo", "dev", "new.yml", null));

        assertThrows(ProfilerRuntimeException.class, () -> service.updateProfiler(stored.getId(), update));

        verify(repository, never()).save(any(Profiler.class));
        verify(awxClient, never()).deleteJobTemplate(OLD_TEMPLATE_ID);
        verify(awxClient, never()).deleteProject(OLD_PROJECT_ID);
        verify(awxClient).deleteJobTemplate(61);
        verify(awxClient).deleteProject(51);
    }

    @Test
    void failedCreationKeepsResourcesThatAlreadyExisted() throws Throwable {
        stubProjects("newRepo", "dev", List.of(project(41, "newRepo")));
        var template = createdTemplate(61);
        when(awxClient.getJobTemplateFromProjectIdAndPlaybook(41, "new.yml"))
                .thenReturn(results(Collections.emptyList()), results(List.of(template)));
        when(awxClient.createJobTemplateAndAddExecuteRoleToDefaultTeam(any(), any(), any(), any(), any()))
                .thenThrow(new AwxProjectUpdateFailedException("failed", null));
        var update = profiler("old", action("newRepo", "dev", "new.yml", null));

        assertThrows(ProfilerRuntimeException.class, () -> service.updateProfiler(stored.getId(), update));

        verify(awxClient).deleteJobTemplate(61);
        verify(awxClient, never()).deleteProject(anyInt());
    }

    @Test
    void failedCreationKeepsProjectFoundBySubstring() throws Throwable {
        stubProjects("https://host/x", "main", List.of(project(41, "https://host/x.git")));
        stubTemplate(41, "play.yml", 61);
        when(awxClient.createJobTemplateAndAddExecuteRoleToDefaultTeam(any(), any(), any(), any(), any()))
                .thenThrow(new AwxProjectUpdateFailedException("failed", null));
        var update = profiler("old", action("https://host/x", "main", "play.yml", null));

        assertThrows(ProfilerRuntimeException.class, () -> service.updateProfiler(stored.getId(), update));

        verifyNothingRemoved();
    }

    @Test
    void failingRollbackStillEndsInProfilerRuntimeException() throws Throwable {
        stubProjects("newRepo", "dev", List.of(), List.of(project(51, "newRepo")));
        stubTemplate(51, "new.yml", 61);
        when(awxClient.createJobTemplateAndAddExecuteRoleToDefaultTeam(any(), any(), any(), any(), any()))
                .thenThrow(new AwxProjectUpdateFailedException("failed", null));
        doThrow(new RuntimeException("template")).when(awxClient).deleteJobTemplate(61);
        var update = profiler("old", action("newRepo", "dev", "new.yml", null));

        assertThrows(ProfilerRuntimeException.class, () -> service.updateProfiler(stored.getId(), update));

        verify(repository, never()).save(any(Profiler.class));
    }

    @Test
    void failingRollbackLookupStillEndsInProfilerRuntimeException() throws Throwable {
        when(awxClient.getProjectFromRepoAndBranch("newRepo", "dev"))
                .thenReturn(results(Collections.emptyList()))
                .thenThrow(new RuntimeException("lookup"));
        when(awxClient.createJobTemplateAndAddExecuteRoleToDefaultTeam(any(), any(), any(), any(), any()))
                .thenThrow(new AwxProjectUpdateFailedException("failed", null));
        var update = profiler("old", action("newRepo", "dev", "new.yml", null));

        assertThrows(ProfilerRuntimeException.class, () -> service.updateProfiler(stored.getId(), update));
    }

    @Test
    void sourceChangeStillSavesWhenRemovingOldResourcesFails() throws Throwable {
        stubExisting("repo", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        stubAbsent("newRepo", "dev");
        stubCreation();
        doThrow(new RuntimeException("template")).when(awxClient).deleteJobTemplate(OLD_TEMPLATE_ID);

        service.updateProfiler(stored.getId(), profiler("old", action("newRepo", "dev", "new.yml", null)));

        verify(awxClient).deleteJobTemplate(OLD_TEMPLATE_ID);
        verify(repository).save(argThat(p -> p.getId().equals(stored.getId())));
    }

    @Test
    void deleteStillPropagatesAwxFailure() throws Throwable {
        stubExisting("repo", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        doThrow(new RuntimeException("project")).when(awxClient).deleteProject(OLD_PROJECT_ID);

        assertThrows(RuntimeException.class, () -> service.deleteProfiler(stored.getId()));

        verify(repository, never()).deleteById(any());
    }

    @Test
    void sourceChangeStillReturnsSavedProfilerWhenSharedResourceLookupFails() throws Throwable {
        stubAbsent("newRepo", "dev");
        stubCreation();
        when(repository.findAll()).thenThrow(new RuntimeException("database lookup"));
        var update = profiler("old", action("newRepo", "dev", "new.yml", null));

        var result = assertDoesNotThrow(() -> service.updateProfiler(stored.getId(), update));

        assertSame(update, result);
        verify(repository).save(update);
        verifyNothingRemoved();
    }

    @Test
    void sourceChangeKeepsResourcesSharedWithOtherProfiler() throws Throwable {
        stubExisting("repo", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        stubAbsent("newRepo", "dev");
        stubCreation();
        var other = profiler("other", action("repo", "main", "play.yml", null));
        when(repository.findAll()).thenReturn(List.of(stored, other));

        service.updateProfiler(stored.getId(), profiler("old", action("newRepo", "dev", "new.yml", null)));

        verifyNothingRemoved();
        verify(repository).save(any(Profiler.class));
    }

    @Test
    void sourceChangeKeepsProjectSharedWithOtherPlaybook() throws Throwable {
        stubExisting("repo", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        stubAbsent("newRepo", "dev");
        stubCreation();
        var other = profiler("other", action("repo", "main", "other.yml", null));
        when(repository.findAll()).thenReturn(List.of(stored, other));

        service.updateProfiler(stored.getId(), profiler("old", action("newRepo", "dev", "new.yml", null)));

        verify(awxClient).deleteJobTemplate(OLD_TEMPLATE_ID);
        verify(awxClient, never()).deleteProject(anyInt());
    }

    @Test
    void deleteKeepsResourcesSharedWithOtherProfiler() throws Throwable {
        stubExisting("repo", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        var other = profiler("other", action("repo", "main", "play.yml", null));
        when(repository.findAll()).thenReturn(List.of(stored, other));

        service.deleteProfiler(stored.getId());

        verifyNothingRemoved();
        verify(repository).deleteById(stored.getId());
    }

    @Test
    void deleteKeepsResourcesOtherProfilerResolvesToBySubstring() throws Throwable {
        ((AwxAction) stored.getAction()).setAwxRepo("https://host/x.git");
        stubExisting("https://host/x.git", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        stubProjects("https://host/x", "main", List.of(project(OLD_PROJECT_ID, "https://host/x.git")));
        var other = profiler("other", action("https://host/x", "main", "play.yml", null));
        when(repository.findAll()).thenReturn(List.of(stored, other));

        service.deleteProfiler(stored.getId());

        verifyNothingRemoved();
        verify(repository).deleteById(stored.getId());
    }

    @Test
    void deleteRemovesUnsharedResources() throws Throwable {
        stubExisting("repo", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);

        service.deleteProfiler(stored.getId());

        verify(awxClient).deleteJobTemplate(OLD_TEMPLATE_ID);
        verify(awxClient).deleteProject(OLD_PROJECT_ID);
        verify(repository).deleteById(stored.getId());
    }

    @Test
    void storedProfilerWithoutActionDoesNotBreakDelete() throws Throwable {
        stubExisting("repo", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        when(repository.findAll()).thenReturn(List.of(stored, profiler("broken", null)));

        service.deleteProfiler(stored.getId());

        verify(awxClient).deleteJobTemplate(OLD_TEMPLATE_ID);
        verify(repository).deleteById(stored.getId());
    }

    @Test
    void storedProfilerWithoutActionCanBeDeleted() {
        var broken = profiler("broken", null);
        when(repository.findById(broken.getId())).thenReturn(java.util.Optional.of(broken));

        service.deleteProfiler(broken.getId());

        verifyNoInteractions(awxClient);
        verify(repository).deleteById(broken.getId());
    }

    @Test
    void storedProfilerWithoutActionDoesNotBreakUpdateOfOtherProfiler() throws Throwable {
        stubExisting("repo", "main", "play.yml", OLD_PROJECT_ID, OLD_TEMPLATE_ID);
        stubAbsent("newRepo", "dev");
        stubCreation();
        when(repository.findAll()).thenReturn(List.of(stored, profiler("broken", null)));

        service.updateProfiler(stored.getId(), profiler("old", action("newRepo", "dev", "new.yml", null)));

        verify(awxClient).deleteJobTemplate(OLD_TEMPLATE_ID);
    }

    @Test
    void storedProfilerWithoutActionCanBeUpdated() throws Throwable {
        var broken = profiler("broken", null);
        when(repository.findById(broken.getId())).thenReturn(java.util.Optional.of(broken));
        stubAbsent("newRepo", "dev");
        stubCreation();

        service.updateProfiler(broken.getId(), profiler("fixed", action("newRepo", "dev", "new.yml", null)));

        verify(awxClient).createJobTemplateAndAddExecuteRoleToDefaultTeam(eq("newRepo"), eq("dev"), eq("new.yml"), anyList(), any());
        verifyNothingRemoved();
        verify(repository).save(argThat(p -> p.getId().equals(broken.getId())));
    }

    @Test
    void updateUnknownIdThrowsNotFound() {
        var unknown = UUID.randomUUID();
        when(repository.findById(unknown)).thenReturn(java.util.Optional.empty());

        assertThrows(ProfilerNotFoundException.class,
                () -> service.updateProfiler(unknown, profiler("x", action("repo", "main", "play.yml", null))));

        verify(repository, never()).save(any(Profiler.class));
    }

    @Test
    void updateWithoutActionIsRejected() {
        assertThrows(ProfilerBadRequestException.class,
                () -> service.updateProfiler(stored.getId(), profiler("x", null)));

        verifyNoInteractions(awxClient);
        verify(repository, never()).save(any(Profiler.class));
    }
}
