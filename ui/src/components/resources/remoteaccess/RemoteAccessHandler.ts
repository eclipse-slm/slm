import PlatformManagementClient from '@/api/platform-management/platform-management-client';
import ResourceManagementClient from '@/api/resource-management/resource-management-client';
import { Credential, CredentialCreateRequest } from '@/api/platform-management/client';
import { ResourceCredentialScope, RemoteAccessCreateDTO, ConnectionType } from '@/api/resource-management/client';
import {RemoteAccessFormData} from "@/components/resources/remoteaccess/RemoteAccessTypes";
import logRequestError from "@/api/restApiHelper";
import {CredentialFormData} from "@/components/credentials/CredentialTypes";

export function remoteAccessHandler() {

  async function createCredentialForRemoteAccess(fullPathUserGroupId: string, credentialFormData: CredentialFormData) {
    if (!credentialFormData?.isFormValid) {
      throw new Error('Credential data missing');
    }
    const credentialId = globalThis.crypto?.randomUUID?.();
    if (!credentialId) {
      throw new Error('Unable to generate credential id');
    }
    const request: CredentialCreateRequest = {
      entityLinks: [],
      fullPathOwnerGroupId: fullPathUserGroupId,
      credential: {
        id: credentialId,
        name: credentialFormData.formData.credentialName,
        scopesRaw: [ResourceCredentialScope.RemoteAccess],
        data: credentialFormData.formData.data,
      } as Credential,
    } as CredentialCreateRequest;
    try {
      await PlatformManagementClient.credentialsApi.createOrUpdateCredential(credentialId, request);
    } catch (e) {
      logRequestError(e);
      throw e;
    }
    return credentialId;
  }

  async function addRemoteAccessWithExistingCredential(resourceId: string, fullPathUserGroupId: string, remoteAccessFormData: any, credentialId: string) {
    const remoteAccessCreateDTO = {
      fullPathOwnerGroupId: fullPathUserGroupId,
      credentialId: credentialId,
      username: remoteAccessFormData.remoteAccessUsername || undefined,
      connectionType: remoteAccessFormData.connectionTypeName as ConnectionType,
      connectionPort: remoteAccessFormData.connectionPort as number,
    } as RemoteAccessCreateDTO as any;
    return ResourceManagementClient.resourcesApi.addRemoteAccessForResource(
      resourceId,
      remoteAccessCreateDTO
    );
  }

  async function addRemoteAccess(resourceId: string, fullPathUserGroupId: string, remoteAccessFormData: RemoteAccessFormData) {
    if (!remoteAccessFormData.formData.credentialFormData?.isFormValid) {
      throw new Error('Credential form is not valid');
    }
    if (remoteAccessFormData.formData.credentialFormData.formData.useExisting) {
      const existingCredentialId = remoteAccessFormData.formData.credentialFormData.formData.existingCredentialId;
      if (!existingCredentialId) {
        throw new Error('Existing credential is required');
      }
      await addRemoteAccessWithExistingCredential(resourceId, fullPathUserGroupId, remoteAccessFormData.formData, existingCredentialId);
    } else {
      const createdCredentialId = await createCredentialForRemoteAccess(fullPathUserGroupId, remoteAccessFormData.formData.credentialFormData);
      await addRemoteAccessWithExistingCredential(resourceId, fullPathUserGroupId, remoteAccessFormData.formData, createdCredentialId);
    }
  }

  return {
    createCredentialForRemoteAccess,
    addRemoteAccessWithExistingCredential,
    addRemoteAccess,
  };
}
