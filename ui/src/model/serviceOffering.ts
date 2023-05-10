import {UUID} from "vue-uuid";

export default class ServiceOffering {
    id: UUID;
    name: string;
    description: string;
    shortDescription: string;
    coverImage: string;
    serviceCategoryId: bigint;
    versions: string;
    serviceVendorId: UUID;
}
