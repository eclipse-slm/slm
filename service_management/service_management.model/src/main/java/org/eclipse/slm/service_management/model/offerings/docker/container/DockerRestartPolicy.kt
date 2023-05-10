package org.eclipse.slm.service_management.model.offerings.docker.container

enum class DockerRestartPolicy(val value: String) {
    NO("no"),
    ALWAYS("always"),
    ON_FAILURE("on-failure"),
    UNLESS_STOPPED("unless-stopped");
}
