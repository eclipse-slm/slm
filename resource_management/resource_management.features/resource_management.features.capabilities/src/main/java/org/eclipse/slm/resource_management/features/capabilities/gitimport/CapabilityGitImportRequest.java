package org.eclipse.slm.resource_management.features.capabilities.gitimport;

/**
 * Location of a capability definition file in a git repository.
 *
 * @param repositoryUrl http(s) URL of the repository
 * @param branch        branch or tag; blank means {@code main}
 * @param filePath      path of the file inside the repository; blank means {@code capability.json}
 */
public record CapabilityGitImportRequest(String repositoryUrl, String branch, String filePath) {
}
