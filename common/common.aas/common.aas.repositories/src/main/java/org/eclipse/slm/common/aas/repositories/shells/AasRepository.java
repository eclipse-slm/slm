package org.eclipse.slm.common.aas.repositories.shells;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.basyx.core.exceptions.CollidingIdentifierException;
import org.eclipse.digitaltwin.basyx.core.exceptions.ElementDoesNotExistException;
import org.eclipse.digitaltwin.basyx.core.exceptions.MissingIdentifierException;
import org.eclipse.digitaltwin.basyx.core.pagination.CursorResult;
import org.eclipse.digitaltwin.basyx.core.pagination.PaginationInfo;


public interface AasRepository {

	CursorResult<List<AssetAdministrationShell>> getAllAas(List<SpecificAssetId> assetIds, String idShort, PaginationInfo pInfo);

	AssetAdministrationShell getAas(String aasId) throws ElementDoesNotExistException;

	void createAas(AssetAdministrationShell aas) throws CollidingIdentifierException, MissingIdentifierException;

	void deleteAas(String aasId);

	void updateAas(String aasId, AssetAdministrationShell aas);

	CursorResult<List<Reference>> getSubmodelReferences(String aasId, PaginationInfo pInfo);

	void addSubmodelReference(String aasId, Reference submodelReference);

	void removeSubmodelReference(String aasId, String submodelId);

	void setAssetInformation(String aasId, AssetInformation aasInfo) throws ElementDoesNotExistException;

	AssetInformation getAssetInformation(String aasId) throws ElementDoesNotExistException;

	File getThumbnail(String aasId);

	void setThumbnail(String aasId, String fileName, String contentType, InputStream inputStream);

	void deleteThumbnail(String aasId);

	default String getName() {
		return "aas-repo";
	}
	
}
