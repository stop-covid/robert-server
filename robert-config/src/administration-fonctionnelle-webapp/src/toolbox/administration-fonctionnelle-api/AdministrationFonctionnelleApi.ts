import axios from 'axios';
import {API_BASE_URL} from "../../constantes/Environments";


class AdministrationFonctionnelleApi {


    static async GET(
        path: string,
        header = {headers: {'Content-Type': 'application/json'}}
    ) {
        const url = new URL(API_BASE_URL.concat(path)).toString();
        console.log('trying to reach : ' + url);
        return axios.get(url, header);
    }

    static async POST(
        path: string,
        data : any,
        header = {headers: {'Content-Type': 'application/json'}}
    ) {
        const url = new URL(API_BASE_URL.concat(path)).toString();
        console.log('trying to reach : ' + url);
        return axios.put(url, data, header);
    }

    static async PUT(
        path: string,
        data : any,
        header = {headers: {'Content-Type': 'application/json'}}
    ) {
        const url = new URL(API_BASE_URL.concat(path)).toString();
        console.log('trying to reach : ' + url);
        return axios.put(url, data, header);
    }
}

export default AdministrationFonctionnelleApi;
