import axios from "axios";
import {
     CredentialsApi,
     type UserCreateRequest,
} from "@/api/platform-management/client";

export interface UserOverviewResponse {
    username: string;
    firstName: string;
    lastName: string;
    email: string;
    admin: boolean;
}

class PlatformManagementClient{

    apiUrl = "/platform-management";

    credentialsApi = new CredentialsApi(undefined, this.apiUrl);

    async getUsers(): Promise<UserOverviewResponse[]> {
        return axios
            .get<UserOverviewResponse[]>(`${this.apiUrl}/users`)
            .then(response => response.data);
    }

    async createUser(userCreateRequest: UserCreateRequest): Promise<void> {
        return axios
            .post(`${this.apiUrl}/users`, userCreateRequest)
            .then(() => undefined);
    }

    async makeUserAdmin(username: string): Promise<void> {
        return axios
            .post(`${this.apiUrl}/users/${encodeURIComponent(username)}/admin`)
            .then(() => undefined);
    }

    async deleteUser(username: string): Promise<void> {
        return axios
            .delete(`${this.apiUrl}/users/${encodeURIComponent(username)}`)
            .then(() => undefined);
    }
}

export default new PlatformManagementClient()