# Release Process

## Capabilities & Profilers
Ensure that all capabilities and profilers initialized via the [Resource Management Initializer](/resource_management/resource_management.service/resource_management.service.initializer/src/main/resources/init)
use tagged versions rather than branch names. This ensures that  the correct versions are used when creating releases and that changes made to the capabilities 
and profilers after the release do not affect the released version.

## AWX Execution Environment
* Create a release for the [AWX Execution Environment](https://github.com/eclipse-slm/awx-execution-environment) by updating the version in the Github Actions
  Workflow and creating a tag.
* Update the version of the AWX Execution Environment in the core repository to the new release version (e.g., X.X.X-RELEASE) of the AWX Execution Environment.
* Set the version of the AWX Execution Environment to the next SNAPSHOT version (e.g., X.X.X-SNAPSHOT)

## Release Version
+ Create a new release branch from the `develop` branch with the name `release/X.X.X-RELEASE`, where X.X.X is the version number of the release.
* Update the version number in all relevant files (e.g., pom.xml, etc.) from X.X.X-SNAPSHOT to X.X.X-RELEASE.
* Once the release is complete, merge the release branch back into the `main` branch and create a tag for the release (e.g., X.X.X-RELEASE).
* After merging the release branch into `main`, merge it into `develop` and update the version number in the `develop` branch to the next SNAPSHOT 
  version (e.g., X.X.X-SNAPSHOT).
* Create a new release in the GitHub repository for the [core project](https://github.com/eclipse-slm/slm/releases) with the tag X.X.X-RELEASE and include 
release notes that summarize the changes and improvements made in the release.