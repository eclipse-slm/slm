package org.eclipse.slm.service_management.service.rest.mocks;

public class AwxMockedResponses {

    public final static String GET_PROJECT_RESPONSE_JSON = """
        {
          "count": 1,
          "next": null,
          "previous": null,
          "results": [
            {
              "id": 116,
              "type": "project",
              "url": "/api/v2/projects/116/",
              "related": {
                "created_by": "/api/v2/users/1/",
                "modified_by": "/api/v2/users/1/",
                "credential": "/api/v2/credentials/4/",
                "last_job": "/api/v2/project_updates/12185/",
                "teams": "/api/v2/projects/116/teams/",
                "playbooks": "/api/v2/projects/116/playbooks/",
                "inventory_files": "/api/v2/projects/116/inventories/",
                "update": "/api/v2/projects/116/update/",
                "project_updates": "/api/v2/projects/116/project_updates/",
                "scm_inventory_sources": "/api/v2/projects/116/scm_inventory_sources/",
                "schedules": "/api/v2/projects/116/schedules/",
                "activity_stream": "/api/v2/projects/116/activity_stream/",
                "notification_templates_started": "/api/v2/projects/116/notification_templates_started/",
                "notification_templates_success": "/api/v2/projects/116/notification_templates_success/",
                "notification_templates_error": "/api/v2/projects/116/notification_templates_error/",
                "access_list": "/api/v2/projects/116/access_list/",
                "object_roles": "/api/v2/projects/116/object_roles/",
                "copy": "/api/v2/projects/116/copy/",
                "organization": "/api/v2/organizations/1/",
                "last_update": "/api/v2/project_updates/12185/"
              },
              "summary_fields": {
                "organization": {
                  "id": 1,
                  "name": "Default",
                  "description": ""
                },
                "credential": {
                  "id": 4,
                  "name": "Codebeamer",
                  "description": "",
                  "kind": "scm",
                  "cloud": false,
                  "kubernetes": false,
                  "credential_type_id": 2
                },
                "last_job": {
                  "id": 12185,
                  "name": "Docker",
                  "description": "",
                  "finished": "2021-10-27T12:52:58.647099Z",
                  "status": "failed",
                  "failed": true
                },
                "last_update": {
                  "id": 12185,
                  "name": "Docker",
                  "description": "",
                  "status": "failed",
                  "failed": true
                },
                "created_by": {
                  "id": 1,
                  "username": "admin",
                  "first_name": "",
                  "last_name": ""
                },
                "modified_by": {
                  "id": 1,
                  "username": "admin",
                  "first_name": "",
                  "last_name": ""
                },
                "object_roles": {
                  "admin_role": {
                    "description": "Can manage all aspects of the project",
                    "name": "Admin",
                    "id": 538
                  },
                  "use_role": {
                    "description": "Can use the project in a job template",
                    "name": "Use",
                    "id": 539
                  },
                  "update_role": {
                    "description": "May update the project",
                    "name": "Update",
                    "id": 540
                  },
                  "read_role": {
                    "description": "May view settings for the project",
                    "name": "Read",
                    "id": 541
                  }
                },
                "user_capabilities": {
                  "edit": false,
                  "delete": false,
                  "start": false,
                  "schedule": false,
                  "copy": false
                }
              },
              "created": "2021-09-22T16:44:47.617830Z",
              "modified": "2021-09-22T16:58:19.601046Z",
              "name": "Docker",
              "description": "",
              "local_path": "_116__docker",
              "scm_type": "git",
              "scm_url": "https://cb.ipa.fraunhofer.de/cb/git/070_ansible_role_docker",
              "scm_branch": "master",
              "scm_refspec": "",
              "scm_clean": true,
              "scm_delete_on_update": true,
              "credential": 4,
              "timeout": 0,
              "scm_revision": "55d934b149fdab3a209f6b6076d86a8603ef5dcc",
              "last_job_run": "2021-10-27T12:52:58.647099Z",
              "last_job_failed": true,
              "next_job_run": null,
              "status": "failed",
              "organization": 1,
              "scm_update_on_launch": true,
              "scm_update_cache_timeout": 0,
              "allow_override": false,
              "custom_virtualenv": null,
              "last_update_failed": true,
              "last_updated": "2021-10-27T12:52:58.647099Z"
            }
          ]
        }
    """;

    public final static String GET_JOB_TEMPLATE_RESPONSE_JSON = """
        {
          "count": 1,
          "next": null,
          "previous": null,
          "results": [
            {
              "id": 117,
              "type": "job_template",
              "url": "/api/v2/job_templates/117/",
              "related": {
                "created_by": "/api/v2/users/1/",
                "modified_by": "/api/v2/users/1/",
                "labels": "/api/v2/job_templates/117/labels/",
                "inventory": "/api/v2/inventories/7/",
                "project": "/api/v2/projects/116/",
                "organization": "/api/v2/organizations/1/",
                "credentials": "/api/v2/job_templates/117/credentials/",
                "last_job": "/api/v2/jobs/12156/",
                "jobs": "/api/v2/job_templates/117/jobs/",
                "schedules": "/api/v2/job_templates/117/schedules/",
                "activity_stream": "/api/v2/job_templates/117/activity_stream/",
                "launch": "/api/v2/job_templates/117/launch/",
                "webhook_key": "/api/v2/job_templates/117/webhook_key/",
                "webhook_receiver": "",
                "notification_templates_started": "/api/v2/job_templates/117/notification_templates_started/",
                "notification_templates_success": "/api/v2/job_templates/117/notification_templates_success/",
                "notification_templates_error": "/api/v2/job_templates/117/notification_templates_error/",
                "access_list": "/api/v2/job_templates/117/access_list/",
                "survey_spec": "/api/v2/job_templates/117/survey_spec/",
                "object_roles": "/api/v2/job_templates/117/object_roles/",
                "instance_groups": "/api/v2/job_templates/117/instance_groups/",
                "slice_workflow_jobs": "/api/v2/job_templates/117/slice_workflow_jobs/",
                "copy": "/api/v2/job_templates/117/copy/"
              },
              "summary_fields": {
                "organization": {
                  "id": 1,
                  "name": "Default",
                  "description": ""
                },
                "inventory": {
                  "id": 7,
                  "name": "Basic VM Inventory",
                  "description": "",
                  "has_active_failures": false,
                  "total_hosts": 2,
                  "hosts_with_active_failures": 0,
                  "total_groups": 2,
                  "has_inventory_sources": true,
                  "total_inventory_sources": 1,
                  "inventory_sources_with_failures": 0,
                  "organization_id": 1,
                  "kind": ""
                },
                "project": {
                  "id": 116,
                  "name": "Docker",
                  "description": "",
                  "status": "failed",
                  "scm_type": "git"
                },
                "last_job": {
                  "id": 12156,
                  "name": "Docker Container - Deploy",
                  "description": "",
                  "finished": "2021-10-26T12:01:28.386546Z",
                  "status": "failed",
                  "failed": true
                },
                "last_update": {
                  "id": 12156,
                  "name": "Docker Container - Deploy",
                  "description": "",
                  "status": "failed",
                  "failed": true
                },
                "created_by": {
                  "id": 1,
                  "username": "admin",
                  "first_name": "",
                  "last_name": ""
                },
                "modified_by": {
                  "id": 1,
                  "username": "admin",
                  "first_name": "",
                  "last_name": ""
                },
                "object_roles": {
                  "admin_role": {
                    "description": "Can manage all aspects of the job template",
                    "name": "Admin",
                    "id": 542
                  },
                  "execute_role": {
                    "description": "May run the job template",
                    "name": "Execute",
                    "id": 543
                  },
                  "read_role": {
                    "description": "May view settings for the job template",
                    "name": "Read",
                    "id": 544
                  }
                },
                "user_capabilities": {
                  "edit": false,
                  "delete": false,
                  "start": true,
                  "schedule": true,
                  "copy": false
                },
                "labels": {
                  "count": 0,
                  "results": []
                },
                "recent_jobs": [
                  {
                    "id": 12156,
                    "status": "failed",
                    "finished": "2021-10-26T12:01:28.386546Z",
                    "canceled_on": null,
                    "type": "job"
                  },
                  {
                    "id": 11517,
                    "status": "successful",
                    "finished": "2021-10-06T16:23:17.283644Z",
                    "canceled_on": null,
                    "type": "job"
                  },
                  {
                    "id": 11515,
                    "status": "successful",
                    "finished": "2021-10-06T16:20:09.256078Z",
                    "canceled_on": null,
                    "type": "job"
                  },
                  {
                    "id": 11513,
                    "status": "failed",
                    "finished": "2021-10-06T16:17:41.101669Z",
                    "canceled_on": null,
                    "type": "job"
                  },
                  {
                    "id": 11511,
                    "status": "failed",
                    "finished": "2021-10-06T16:15:55.533273Z",
                    "canceled_on": null,
                    "type": "job"
                  },
                  {
                    "id": 11509,
                    "status": "successful",
                    "finished": "2021-10-06T16:12:30.798591Z",
                    "canceled_on": null,
                    "type": "job"
                  },
                  {
                    "id": 11506,
                    "status": "failed",
                    "finished": "2021-10-06T16:10:56.552711Z",
                    "canceled_on": null,
                    "type": "job"
                  },
                  {
                    "id": 11504,
                    "status": "failed",
                    "finished": "2021-10-06T16:08:45.914704Z",
                    "canceled_on": null,
                    "type": "job"
                  },
                  {
                    "id": 11502,
                    "status": "successful",
                    "finished": "2021-10-06T16:06:28.833451Z",
                    "canceled_on": null,
                    "type": "job"
                  },
                  {
                    "id": 11500,
                    "status": "successful",
                    "finished": "2021-10-06T16:02:57.292568Z",
                    "canceled_on": null,
                    "type": "job"
                  }
                ],
                "credentials": [
                  {
                    "id": 3,
                    "name": "Ansible Vault",
                    "description": "",
                    "kind": "vault",
                    "cloud": false
                  },
                  {
                    "id": 5,
                    "name": "vCenter",
                    "description": "",
                    "kind": "vmware",
                    "cloud": true
                  }
                ]
              },
              "created": "2021-09-22T17:16:10.613567Z",
              "modified": "2021-09-24T22:09:30.683121Z",
              "name": "Docker Container - Deploy",
              "description": "",
              "job_type": "run",
              "inventory": 7,
              "project": 116,
              "playbook": "docker_container_deploy.yml",
              "scm_branch": "",
              "forks": 0,
              "limit": "",
              "verbosity": 3,
              "extra_vars": "",
              "job_tags": "",
              "force_handlers": false,
              "skip_tags": "",
              "start_at_task": "",
              "timeout": 0,
              "use_fact_cache": false,
              "organization": 1,
              "last_job_run": "2021-10-26T12:01:28.386546Z",
              "last_job_failed": true,
              "next_job_run": null,
              "status": "failed",
              "host_config_key": "",
              "ask_scm_branch_on_launch": false,
              "ask_diff_mode_on_launch": false,
              "ask_variables_on_launch": true,
              "ask_limit_on_launch": false,
              "ask_tags_on_launch": false,
              "ask_skip_tags_on_launch": false,
              "ask_job_type_on_launch": false,
              "ask_verbosity_on_launch": false,
              "ask_inventory_on_launch": false,
              "ask_credential_on_launch": false,
              "survey_enabled": false,
              "become_enabled": false,
              "diff_mode": false,
              "allow_simultaneous": false,
              "custom_virtualenv": null,
              "job_slice_count": 1,
              "webhook_service": "",
              "webhook_credential": null
            }
          ]
        }
    """;

    public final static String POST_JOB_RUN_RESPONSE_JSON = """
        {
            "job": 327,
            "ignored_fields": {},
            "id": 327,
            "type": "job",
            "url": "/api/v2/jobs/327/",
            "related": {
                "created_by": "/api/v2/users/1/",
                "modified_by": "/api/v2/users/1/",
                "labels": "/api/v2/jobs/327/labels/",
                "inventory": "/api/v2/inventories/2/",
                "project": "/api/v2/projects/13/",
                "organization": "/api/v2/organizations/2/",
                "credentials": "/api/v2/jobs/327/credentials/",
                "unified_job_template": "/api/v2/job_templates/14/",
                "stdout": "/api/v2/jobs/327/stdout/",
                "job_events": "/api/v2/jobs/327/job_events/",
                "job_host_summaries": "/api/v2/jobs/327/job_host_summaries/",
                "activity_stream": "/api/v2/jobs/327/activity_stream/",
                "notifications": "/api/v2/jobs/327/notifications/",
                "create_schedule": "/api/v2/jobs/327/create_schedule/",
                "job_template": "/api/v2/job_templates/14/",
                "cancel": "/api/v2/jobs/327/cancel/",
                "relaunch": "/api/v2/jobs/327/relaunch/"
            },
            "summary_fields": {
                "organization": {
                    "id": 2,
                    "name": "Self Service Portal",
                    "description": ""
                },
                "inventory": {
                    "id": 2,
                    "name": "Empty Inventory",
                    "description": "",
                    "has_active_failures": false,
                    "total_hosts": 0,
                    "hosts_with_active_failures": 0,
                    "total_groups": 0,
                    "has_inventory_sources": false,
                    "total_inventory_sources": 0,
                    "inventory_sources_with_failures": 0,
                    "organization_id": 2,
                    "kind": ""
                },
                "project": {
                    "id": 13,
                    "name": "Docker Swarm",
                    "description": "",
                    "status": "successful",
                    "scm_type": "git"
                },
                "job_template": {
                    "id": 14,
                    "name": "Docker Swarm - Install",
                    "description": ""
                },
                "unified_job_template": {
                    "id": 14,
                    "name": "Docker Swarm - Install",
                    "description": "",
                    "unified_job_type": "job"
                },
                "created_by": {
                    "id": 1,
                    "username": "admin",
                    "first_name": "",
                    "last_name": ""
                },
                "modified_by": {
                    "id": 1,
                    "username": "admin",
                    "first_name": "",
                    "last_name": ""
                },
                "user_capabilities": {
                    "delete": true,
                    "start": true
                },
                "labels": {
                    "count": 0,
                    "results": []
                },
                "credentials": [
                    {
                        "id": 5,
                        "name": "HashiVault",
                        "description": "",
                        "kind": null,
                        "cloud": true
                    }
                ]
            },
            "created": "2021-10-28T12:46:52.379676Z",
            "modified": "2021-10-28T12:46:52.406219Z",
            "name": "Docker Swarm - Install",
            "description": "",
            "job_type": "run",
            "inventory": 2,
            "project": 13,
            "playbook": "create_fabos.yml",
            "scm_branch": "",
            "forks": 0,
            "limit": "",
            "verbosity": 0,
            "extra_vars": "{\\"project\\": \\"asdf\\", \\"cluster_members\\": \\"asdfasdf\\"}",
            "job_tags": "",
            "force_handlers": false,
            "skip_tags": "",
            "start_at_task": "",
            "timeout": 0,
            "use_fact_cache": false,
            "organization": 2,
            "unified_job_template": 14,
            "launch_type": "manual",
            "status": "pending",
            "failed": false,
            "started": null,
            "finished": null,
            "canceled_on": null,
            "elapsed": 0.0,
            "job_args": "",
            "job_cwd": "",
            "job_env": {},
            "job_explanation": "",
            "execution_node": "",
            "controller_node": "",
            "result_traceback": "",
            "event_processing_finished": false,
            "job_template": 14,
            "passwords_needed_to_start": [],
            "allow_simultaneous": false,
            "artifacts": {},
            "scm_revision": "",
            "instance_group": null,
            "diff_mode": false,
            "job_slice_number": 0,
            "job_slice_count": 1,
            "webhook_service": "",
            "webhook_credential": null,
            "webhook_guid": ""
        }
    """;

    public static final String GET_JOB_RESPONSE_JSON = """
        {
          "id": 11517,
          "type": "job",
          "url": "/api/v2/jobs/11517/",
          "related": {
            "created_by": "/api/v2/users/45/",
            "labels": "/api/v2/jobs/11517/labels/",
            "inventory": "/api/v2/inventories/7/",
            "project": "/api/v2/projects/116/",
            "organization": "/api/v2/organizations/1/",
            "credentials": "/api/v2/jobs/11517/credentials/",
            "unified_job_template": "/api/v2/job_templates/117/",
            "stdout": "/api/v2/jobs/11517/stdout/",
            "job_events": "/api/v2/jobs/11517/job_events/",
            "job_host_summaries": "/api/v2/jobs/11517/job_host_summaries/",
            "activity_stream": "/api/v2/jobs/11517/activity_stream/",
            "notifications": "/api/v2/jobs/11517/notifications/",
            "create_schedule": "/api/v2/jobs/11517/create_schedule/",
            "job_template": "/api/v2/job_templates/117/",
            "cancel": "/api/v2/jobs/11517/cancel/",
            "relaunch": "/api/v2/jobs/11517/relaunch/"
          },
          "summary_fields": {
            "organization": {
              "id": 1,
              "name": "Default",
              "description": ""
            },
            "inventory": {
              "id": 7,
              "name": "Basic VM Inventory",
              "description": "",
              "has_active_failures": false,
              "total_hosts": 2,
              "hosts_with_active_failures": 0,
              "total_groups": 2,
              "has_inventory_sources": true,
              "total_inventory_sources": 1,
              "inventory_sources_with_failures": 0,
              "organization_id": 1,
              "kind": ""
            },
            "project": {
              "id": 116,
              "name": "Docker",
              "description": "",
              "status": "failed",
              "scm_type": "git"
            },
            "job_template": {
              "id": 117,
              "name": "Docker Container - Deploy",
              "description": ""
            },
            "unified_job_template": {
              "id": 117,
              "name": "Docker Container - Deploy",
              "description": "",
              "unified_job_type": "job"
            },
            "instance_group": {
              "id": 1,
              "name": "tower",
              "is_containerized": false
            },
            "created_by": {
              "id": 45,
              "username": "maes@ipa.fhg.de",
              "first_name": "Matthias",
              "last_name": "Schneider"
            },
            "user_capabilities": {
              "delete": false,
              "start": true
            },
            "labels": {
              "count": 0,
              "results": []
            },
            "credentials": [
              {
                "id": 3,
                "name": "Ansible Vault",
                "description": "",
                "kind": "vault",
                "cloud": false
              },
              {
                "id": 5,
                "name": "vCenter",
                "description": "",
                "kind": "vmware",
                "cloud": true
              }
            ]
          },
          "created": "2021-10-06T16:22:24.697771Z",
          "modified": "2021-10-06T16:22:50.596535Z",
          "name": "Docker Container - Deploy",
          "description": "",
          "job_type": "run",
          "inventory": 7,
          "project": 116,
          "playbook": "docker_container_deploy.yml",
          "scm_branch": "",
          "forks": 0,
          "limit": "",
          "verbosity": 3,
          "extra_vars": "{\\"docker_host_ip\\": \\"10.3.11.19\\", \\"docker_host_name\\": \\"BIG-linux-9ab1488f\\", \\"docker_image\\": \\"nginx\\", \\"project_abbreviation\\": \\"BIG\\", \\"service_owner\\": \\"maes\\", \\"service_uuid\\": \\"f521477b-fbab-4f8d-8e3d-1eb56e397972\\", \\"environment_variables\\": {\\"env1\\": \\"val1\\", \\"env2\\": \\"val2\\"}, \\"volumes\\": [\\"/host/path1:/container/path1\\", \\"/host/path2:/container/path2\\"], \\"ports\\": [\\"5555:5555\\", \\"4444:4444\\"], \\"labels\\": {\\"label1\\": \\"val1\\", \\"label2\\": \\"val2\\"}}",
          "job_tags": "",
          "force_handlers": false,
          "skip_tags": "",
          "start_at_task": "",
          "timeout": 0,
          "use_fact_cache": false,
          "organization": 1,
          "unified_job_template": 117,
          "launch_type": "manual",
          "status": "successful",
          "failed": false,
          "started": "2021-10-06T16:22:50.645064Z",
          "finished": "2021-10-06T16:23:17.283644Z",
          "canceled_on": null,
          "elapsed": 26.639,
          "job_args": "[\\"ansible-playbook\\", \\"-u\\", \\"root\\", \\"--ask-vault-pass\\", \\"-vvv\\", \\"-i\\", \\"/tmp/awx_11517_jac6ispa/tmp36025h45\\", \\"-e\\", \\"@/tmp/awx_11517_jac6ispa/env/extravars\\", \\"docker_container_deploy.yml\\"]",
          "job_cwd": "/tmp/awx_11517_jac6ispa/project",
          "job_env": {
            "LC_ALL": "en_US.UTF-8",
            "SUPERVISOR_WEB_CONFIG_PATH": "/etc/supervisord.conf",
            "LANG": "en_US.UTF-8",
            "HOSTNAME": "awx",
            "VAULT_SKIP_VERIFY": "false",
            "VAULT_SECRET_ID_MGMT_AWX_INVENTORY_SCRIPT": "**********",
            "PWD": "/",
            "HOME": "/var/lib/awx",
            "VAULT_ROLE_ID_MGMT_AWX_INVENTORY_SCRIPT": "mgmt-awx-inventory-script",
            "VAULT_ADDR": "https://10.1.4.171",
            "SHLVL": "1",
            "LANGUAGE": "en_US:en",
            "PATH": "/var/lib/awx/venv/ansible/bin:/usr/pgsql-10/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
            "_": "/usr/local/bin/supervisord",
            "SUPERVISOR_ENABLED": "1",
            "SUPERVISOR_SERVER_URL": "unix:///var/run/supervisor/supervisor.sock",
            "SUPERVISOR_PROCESS_NAME": "dispatcher",
            "SUPERVISOR_GROUP_NAME": "tower-processes",
            "DJANGO_SETTINGS_MODULE": "awx.settings.production",
            "DJANGO_LIVE_TEST_SERVER_ADDRESS": "localhost:9013-9199",
            "TZ": "UTC",
            "ANSIBLE_FACT_CACHE_TIMEOUT": "0",
            "ANSIBLE_FORCE_COLOR": "True",
            "ANSIBLE_HOST_KEY_CHECKING": "False",
            "ANSIBLE_INVENTORY_UNPARSED_FAILED": "True",
            "ANSIBLE_PARAMIKO_RECORD_HOST_KEYS": "False",
            "ANSIBLE_VENV_PATH": "/var/lib/awx/venv/ansible",
            "ANSIBLE_PYTHON_INTERPRETER": "/usr/local/bin/python3.8",
            "AWX_PRIVATE_DATA_DIR": "/tmp/awx_11517_jac6ispa",
            "VIRTUAL_ENV": "/var/lib/awx/venv/ansible",
            "PYTHONPATH": "/var/lib/awx/venv/ansible/lib/python3.8/site-packages:",
            "JOB_ID": "11517",
            "INVENTORY_ID": "7",
            "PROJECT_REVISION": "55d934b149fdab3a209f6b6076d86a8603ef5dcc",
            "ANSIBLE_RETRY_FILES_ENABLED": "False",
            "MAX_EVENT_RES": "700000",
            "AWX_HOST": "https://mgmt-awx.infra.fortknox.local",
            "ANSIBLE_SSH_CONTROL_PATH_DIR": "/tmp/awx_11517_jac6ispa/cp",
            "ANSIBLE_COLLECTIONS_PATHS": "/tmp/awx_11517_jac6ispa/requirements_collections:~/.ansible/collections:/usr/share/ansible/collections",
            "ANSIBLE_ROLES_PATH": "/tmp/awx_11517_jac6ispa/requirements_roles:~/.ansible/roles:/usr/share/ansible/roles:/etc/ansible/roles",
            "VMWARE_USER": "mgmt-awx@INFRA.FORTKNOX.LOCAL",
            "VMWARE_PASSWORD": "**********",
            "VMWARE_HOST": "vfkvcenter.infra.fortknox.local",
            "VMWARE_VALIDATE_CERTS": "False",
            "ANSIBLE_CALLBACK_PLUGINS": "/var/lib/awx/venv/awx/lib/python3.8/site-packages/ansible_runner/callbacks",
            "ANSIBLE_STDOUT_CALLBACK": "awx_display",
            "AWX_ISOLATED_DATA_DIR": "/tmp/awx_11517_jac6ispa/artifacts/11517",
            "RUNNER_OMIT_EVENTS": "False",
            "RUNNER_ONLY_FAILED_EVENTS": "False"
          },
          "job_explanation": "",
          "execution_node": "awx",
          "controller_node": "",
          "result_traceback": "",
          "event_processing_finished": true,
          "job_template": 117,
          "passwords_needed_to_start": [],
          "allow_simultaneous": false,
          "artifacts": {},
          "scm_revision": "55d934b149fdab3a209f6b6076d86a8603ef5dcc",
          "instance_group": 1,
          "diff_mode": false,
          "job_slice_number": 0,
          "job_slice_count": 1,
          "webhook_service": "",
          "webhook_credential": null,
          "webhook_guid": "",
          "host_status_counts": {
            "changed": 2
          },
          "playbook_counts": {
            "play_count": 3,
            "task_count": 27
          },
          "custom_virtualenv": "/var/lib/awx/venv/ansible"
        }
    """;
}
