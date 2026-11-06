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

    private void stubExisting(String repo, String branch, String playbook, int projectId, int templateId) {
        var project = mock(Project.class);
        when(project.getId()).thenReturn(projectId);
        var template = mock(JobTemplate.class);
        when(template.getId()).thenReturn(templateId);
        when(awxClient.getProjectFromRepoAndBranch(repo, branch)).thenReturn(results(List.of(project)));
        when(awxClient.getJobTemplateFromProjectIdAndPlaybook(projectId, playbook)).thenReturn(results(List.of(template)));
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
    void sourceChangeCreatesNewBeforeDeletingOld() throws Throwable {
        stubAbsent("newRepo", "dev");
        stubCreation();
        var update = profiler("old", action("newRepo", "dev", "new.yml", null));

        var result = service.updateProfiler(stored.getId(), update);

        assertEquals(stored.getId(), result.getId());
        InOrder inOrder = inOrder(awxClient, repository);
        inOrder.verify(awxClient).createJobTemplateAndAddExecuteRoleToDefaultTeam(eq("newRepo"), eq("dev"), eq("new.yml"), anyList(), any());
        inOrder.verify(awxClient).deleteJobTemplate("repo", "main", "play.yml");
        inOrder.verify(awxClient).deleteProject("repo", "main");
        inOrder.verify(repository).save(argThat(p -> p.getId().equals(stored.getId())));
    }

    @Test
    void sourceChangeWithCredentialsUsesScmCredentialVariant() throws Throwable {
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
        stubAbsent("newRepo", "dev");
        when(awxClient.createJobTemplateAndAddExecuteRoleToDefaultTeam(any(), any(), any(), any(), any()))
                .thenThrow(new AwxProjectUpdateFailedException("failed", null));
        var update = profiler("old", action("newRepo", "dev", "new.yml", null));

        assertThrows(ProfilerRuntimeException.class, () -> service.updateProfiler(stored.getId(), update));

        verify(repository, never()).save(any(Profiler.class));
        verify(awxClient, never()).deleteJobTemplate("repo", "main", "play.yml");
        verify(awxClient, never()).deleteProject("repo", "main");
        verify(awxClient).deleteJobTemplate("newRepo", "dev", "new.yml");
        verify(awxClient).deleteProject("newRepo", "dev");
    }

    @Test
    void failedCreationKeepsResourcesThatAlreadyExisted() throws Throwable {
        var project = mock(Project.class);
        when(project.getId()).thenReturn(41);
        when(awxClient.getProjectFromRepoAndBranch("newRepo", "dev")).thenReturn(results(List.of(project)));
        when(awxClient.getJobTemplateFromProjectIdAndPlaybook(41, "new.yml")).thenReturn(results(Collections.emptyList()));
        when(awxClient.createJobTemplateAndAddExecuteRoleToDefaultTeam(any(), any(), any(), any(), any()))
                .thenThrow(new AwxProjectUpdateFailedException("failed", null));
        var update = profiler("old", action("newRepo", "dev", "new.yml", null));

        assertThrows(ProfilerRuntimeException.class, () -> service.updateProfiler(stored.getId(), update));

        verify(awxClient).deleteJobTemplate("newRepo", "dev", "new.yml");
        verify(awxClient, never()).deleteProject("newRepo", "dev");
    }

    @Test
    void sourceChangeKeepsResourcesSharedWithOtherProfiler() throws Throwable {
        stubAbsent("newRepo", "dev");
        stubCreation();
        var other = profiler("other", action("repo", "main", "play.yml", null));
        when(repository.findAll()).thenReturn(List.of(stored, other));

        service.updateProfiler(stored.getId(), profiler("old", action("newRepo", "dev", "new.yml", null)));

        verify(awxClient, never()).deleteJobTemplate(anyString(), anyString(), anyString());
        verify(awxClient, never()).deleteProject(anyString(), anyString());
        verify(repository).save(any(Profiler.class));
    }

    @Test
    void sourceChangeKeepsProjectSharedWithOtherPlaybook() throws Throwable {
        stubAbsent("newRepo", "dev");
        stubCreation();
        var other = profiler("other", action("repo", "main", "other.yml", null));
        when(repository.findAll()).thenReturn(List.of(stored, other));

        service.updateProfiler(stored.getId(), profiler("old", action("newRepo", "dev", "new.yml", null)));

        verify(awxClient).deleteJobTemplate("repo", "main", "play.yml");
        verify(awxClient, never()).deleteProject(anyString(), anyString());
    }

    @Test
    void deleteKeepsResourcesSharedWithOtherProfiler() throws Throwable {
        var other = profiler("other", action("repo", "main", "play.yml", null));
        when(repository.findAll()).thenReturn(List.of(stored, other));

        service.deleteProfiler(stored.getId());

        verify(awxClient, never()).deleteJobTemplate(anyString(), anyString(), anyString());
        verify(awxClient, never()).deleteProject(anyString(), anyString());
        verify(repository).deleteById(stored.getId());
    }

    @Test
    void deleteRemovesUnsharedResources() throws Throwable {
        service.deleteProfiler(stored.getId());

        verify(awxClient).deleteJobTemplate("repo", "main", "play.yml");
        verify(awxClient).deleteProject("repo", "main");
        verify(repository).deleteById(stored.getId());
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
