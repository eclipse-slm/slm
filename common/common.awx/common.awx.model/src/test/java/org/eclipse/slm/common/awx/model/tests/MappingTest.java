package org.eclipse.slm.common.awx.model.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import org.eclipse.slm.common.awx.model.Organization;
import org.eclipse.slm.common.awx.model.Results;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MappingTest {
    private ObjectMapper objectMapper;

    public String organizationJsonString = """
               {"count":1,"next":null,"previous":null,"results":[{"id":2,"type":"organization","url":"/api/v2/organizations/2/","related":{"created_by":"/api/v2/users/1/","modified_by":"/api/v2/users/1/","projects":"/api/v2/organizations/2/projects/","inventories":"/api/v2/organizations/2/inventories/","job_templates":"/api/v2/organizations/2/job_templates/","workflow_job_templates":"/api/v2/organizations/2/workflow_job_templates/","users":"/api/v2/organizations/2/users/","admins":"/api/v2/organizations/2/admins/","teams":"/api/v2/organizations/2/teams/","credentials":"/api/v2/organizations/2/credentials/","applications":"/api/v2/organizations/2/applications/","activity_stream":"/api/v2/organizations/2/activity_stream/","notification_templates":"/api/v2/organizations/2/notification_templates/","notification_templates_started":"/api/v2/organizations/2/notification_templates_started/","notification_templates_success":"/api/v2/organizations/2/notification_templates_success/","notification_templates_error":"/api/v2/organizations/2/notification_templates_error/","notification_templates_approvals":"/api/v2/organizations/2/notification_templates_approvals/","object_roles":"/api/v2/organizations/2/object_roles/","access_list":"/api/v2/organizations/2/access_list/","instance_groups":"/api/v2/organizations/2/instance_groups/","galaxy_credentials":"/api/v2/organizations/2/galaxy_credentials/"},"summary_fields":{"created_by":{"id":1,"username":"admin","first_name":"","last_name":""},"modified_by":{"id":1,"username":"admin","first_name":"","last_name":""},"object_roles":{"admin_role":{"description":"Can manage all aspects of the organization","name":"Admin","id":19,"user_only":true},"execute_role":{"description":"May run any executable resources in the organization","name":"Execute","id":20},"project_admin_role":{"description":"Can manage all projects of the organization","name":"Project Admin","id":21},"inventory_admin_role":{"description":"Can manage all inventories of the organization","name":"Inventory Admin","id":22},"credential_admin_role":{"description":"Can manage all credentials of the organization","name":"Credential Admin","id":23},"workflow_admin_role":{"description":"Can manage all workflows of the organization","name":"Workflow Admin","id":24},"notification_admin_role":{"description":"Can manage all notifications of the organization","name":"Notification Admin","id":25},"job_template_admin_role":{"description":"Can manage all job templates of the organization","name":"Job Template Admin","id":26},"auditor_role":{"description":"Can view all aspects of the organization","name":"Auditor","id":27},"member_role":{"description":"User is a member of the organization","name":"Member","id":28,"user_only":true},"read_role":{"description":"May view settings for the organization","name":"Read","id":29},"approval_role":{"description":"Can approve or deny a workflow approval node","name":"Approve","id":30}},"user_capabilities":{"edit":true,"delete":true},"related_field_counts":{"inventories":0,"teams":1,"users":0,"job_templates":0,"admins":0,"projects":0}},"created":"2022-01-23T18:52:20.927418Z","modified":"2022-01-23T18:52:20.927441Z","name":"Self Service Portal","description":"","max_hosts":0,"custom_virtualenv":null}]}
                """;

    @BeforeEach
    public void beforeEach() {
        this.objectMapper = new ObjectMapper();
        KotlinModule kotlinModule = new KotlinModule.Builder()
                .nullIsSameAsDefault(true)
                .build();
        this.objectMapper.registerModule(kotlinModule);
    }

    @Test
    public void mapOrganizationFromJsonToObject() throws JsonProcessingException {

        var results = this.objectMapper.readValue(
                organizationJsonString,
                new TypeReference<Results<Organization>>() {}
        );

        return;

    }
}
