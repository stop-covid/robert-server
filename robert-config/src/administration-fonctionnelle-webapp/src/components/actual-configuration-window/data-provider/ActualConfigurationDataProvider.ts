import AdministrationFonctionnelleApi from '../../../toolbox/administration-fonctionnelle-api';
import {
    GET_ACTUAL_CONFIGURATION_ENDPOINT,
    PUT_CONFIGURATION_ENDPOINT
} from '../../../constantes/Environments';

export function getActualConfiguration(): Promise<any | null> {
    return AdministrationFonctionnelleApi.GET(GET_ACTUAL_CONFIGURATION_ENDPOINT)
        .then(response => {
            return response.data;
        })
        .catch(error => {
            console.error(
                'AdministrationFonctionnelleApi?getActualConfiguration --> error : ',
                error
            );
            return null;
        });
}

export function putNewConfiguration(data: any): Promise<any | null> {
    return AdministrationFonctionnelleApi.PUT(PUT_CONFIGURATION_ENDPOINT, data)
        .then(response => {
            console.log(
                'AdministrationFonctionnelleApi?putNewConfiguration --> response : ',
                response.data
            );
            return response.data;
        })
        .catch(error => {
            console.error(
                'AdministrationFonctionnelleApi?putNewConfiguration --> error : ',
                error
            );
            return null;
        });
}
