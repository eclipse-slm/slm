import type { CredentialDataType } from "@/api/platform-management/client";

export interface UsernamePasswordData {
    credentialDataType: CredentialDataType.UsernamePassword;
    username: string;
    password: string;
}

export interface KeyPairData {
    credentialDataType: CredentialDataType.KeyPair;
    publicKey: string;
    privateKey: string;
}

export type CredentialData = UsernamePasswordData | KeyPairData;

export interface CredentialFormData {
    isFormValid: boolean;
    formData: {
        credentialName?: string;
        data?: CredentialData;
        useExisting?: boolean;
        existingCredentialId?: string;
        existingCredentialDataType?: CredentialDataType;
    };
}