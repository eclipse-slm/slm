package org.eclipse.slm.service_management.service.initializer

class GitRepo (
) {
    var name: String = ""

    var url: String = ""

    var gitUsername: String? = null

    var gitPassword: String? = null

    var branchOrTag: String = "main"

    constructor(repoUrl: String) : this() {
        var lastUrlSegment = repoUrl.substring(repoUrl.lastIndexOf('/') + 1);
        if (lastUrlSegment.contains(":")) {
            this.branchOrTag = lastUrlSegment.split(":")[1]
            this.url = repoUrl.substring(0, repoUrl.lastIndexOf(":"))
        }
        else {
            this.url = repoUrl;
        }

        this.name = this.url.substring(this.url.lastIndexOf('/') + 1, this.url.lastIndexOf('.'))

        if (repoUrl.contains("@")) {
            this.url = this.url.substring(repoUrl.lastIndexOf('@') + 1)
            val gitUsernamePasswordSegmentInUrl: String = repoUrl.substring(0, repoUrl.lastIndexOf('@'))
            this.gitUsername = gitUsernamePasswordSegmentInUrl.substring(0, gitUsernamePasswordSegmentInUrl.indexOf(":"))
            this.gitPassword = gitUsernamePasswordSegmentInUrl.substring(gitUsernamePasswordSegmentInUrl.indexOf(":") + 1)
        }
    }

}
