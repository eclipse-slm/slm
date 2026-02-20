export interface DeviceDetailsForm {
  isFormValid: boolean;
  formData: {
    hostname: string;
    ip: string;
    locationId?: string;
    assetId?: string;
    manufacturerName?: string;
    product?: string;
  };
}
