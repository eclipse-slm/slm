package org.eclipse.slm.common.credentials.model;

import org.eclipse.slm.common.credentials.persistence.CredentialEntityLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.SubclassExhaustiveStrategy;
import org.mapstruct.SubclassMapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring", subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION)
public interface CredentialMapper {

    CredentialMapper INSTANCE = Mappers.getMapper(CredentialMapper.class);

    CredentialReadDTO toReadDTO(Credential credential);

    @Mapping(target = "entityLinks", source = "entityLinks")
    CredentialReadDTO toReadDTO(Credential credential, List<CredentialEntityLinkReadDTO> entityLinks);

    CredentialEntityLinkReadDTO toReadDTO(CredentialEntityLink credentialEntityLink);
    List<CredentialEntityLinkReadDTO> toReadDTO(List<CredentialEntityLink> credentialEntityLinks);

    @SubclassMapping(source = CredentialDataUsernamePassword.class, target = CredentialDataUsernamePasswordReadDTO.class)
    @SubclassMapping(source = CredentialDataKeyPair.class, target = CredentialDataKeyPairReadDTO.class)
    CredentialDataReadDTO toReadDTO(CredentialData credentialData);

}
