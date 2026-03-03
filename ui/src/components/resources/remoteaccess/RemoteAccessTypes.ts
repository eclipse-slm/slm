import {CredentialFormData} from "@/components/credentials/CredentialTypes";

export interface RemoteAccessFormData {
    isFormValid: boolean,
    formData: {
        credentialFormData?: CredentialFormData,
        remoteAccessUsername?: string,
        connectionTypeName?: string,
        connectionPort?: number
    }
}