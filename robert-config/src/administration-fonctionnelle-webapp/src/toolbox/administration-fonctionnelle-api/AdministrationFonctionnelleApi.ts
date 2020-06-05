import axios from 'axios';
import { API_BASE_URL } from '../../constantes/Environments';

class AdministrationFonctionnelleApi {
    static async GET(path: string, token: string) {
        AdministrationFonctionnelleApi.handleRequest(token);

        const url = API_BASE_URL.concat(path);
        return axios.get(url);
    }

    static async PUT(path: string, data: any, token: string) {
        AdministrationFonctionnelleApi.handleRequest(token);

        const url = API_BASE_URL.concat(path);
        return axios.put(url, data);
    }

    static handleRequest(token: string) {
        axios.interceptors.request.use(request => {
            request.headers['Content-Type'] = 'application/json';
            request.headers['Authorization'] = `Bearer ${token}`;

            return request;
        });
    }
}

export default AdministrationFonctionnelleApi;
