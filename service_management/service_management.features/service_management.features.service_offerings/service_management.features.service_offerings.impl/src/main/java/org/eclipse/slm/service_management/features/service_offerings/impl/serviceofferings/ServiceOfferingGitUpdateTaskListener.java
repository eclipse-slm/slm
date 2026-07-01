package org.eclipse.slm.service_management.features.service_offerings.impl.serviceofferings;


import java.util.List;

public interface ServiceOfferingGitUpdateTaskListener {

    void onNewTagsDetected(Object sender, List<String> newTags);

    void onTagsDeleted(Object sender, List<String> deletedTags);

}

