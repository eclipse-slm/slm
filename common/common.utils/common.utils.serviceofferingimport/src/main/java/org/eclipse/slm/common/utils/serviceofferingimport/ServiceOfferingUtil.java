package org.eclipse.slm.common.utils.serviceofferingimport;

import org.eclipse.slm.common.utils.files.FilesUtil;
import org.eclipse.slm.common.utils.objectmapper.ObjectMapperUtils;
import org.eclipse.slm.service_management.model.exceptions.ServiceOfferingReferencedFileNotFound;
import org.eclipse.slm.service_management.model.offerings.ServiceCategory;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingCreateOrUpdateRequest;
import org.eclipse.slm.service_management.model.offerings.ServiceOfferingDTOFileImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ServiceOfferingUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceOfferingUtil.class);

    public static ServiceOfferingCreateOrUpdateRequest convertServiceOfferingDTOFileImportToDTOApi(
            ServiceOfferingDTOFileImport serviceOfferingDTOFileImport,
            String initDirectory, List<ServiceCategory> serviceCategories) throws ServiceOfferingReferencedFileNotFound {
        if (!initDirectory.endsWith("/") || !initDirectory.endsWith("\\")) {
            initDirectory = initDirectory + "/";
        }

        var serviceCategoryName = serviceOfferingDTOFileImport.getServiceCategoryName();
        var existingServiceCategory = serviceCategories.stream().filter(c -> c.getName().equals(serviceCategoryName)).findFirst().get();
        byte[] coverImage = new byte[0];
        try {
            coverImage = FilesUtil.loadFileBytes(initDirectory + serviceOfferingDTOFileImport.getCoverImageFilename());
        } catch (IOException e) {
            throw new ServiceOfferingReferencedFileNotFound("Cover image '" + serviceOfferingDTOFileImport.getCoverImageFilename() +"' of service offering '"
                    + initDirectory + "' can't be read");
        }
        var serviceOfferingCreateOrUpdateRequest = ObjectMapperUtils.map(serviceOfferingDTOFileImport, ServiceOfferingCreateOrUpdateRequest.class);
        serviceOfferingCreateOrUpdateRequest.setServiceCategoryId(existingServiceCategory.getId());
        serviceOfferingCreateOrUpdateRequest.setCoverImage(coverImage);

        return serviceOfferingCreateOrUpdateRequest;
    }

}
