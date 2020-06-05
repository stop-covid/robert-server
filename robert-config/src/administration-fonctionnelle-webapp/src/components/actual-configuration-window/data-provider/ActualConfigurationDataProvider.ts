import AdministrationFonctionnelleApi from '../../../toolbox/administration-fonctionnelle-api';
import {
    GET_ACTUAL_CONFIGURATION_ENDPOINT,
    PUT_CONFIGURATION_ENDPOINT
} from '../../../constantes/Environments';

export function getConfiguration(token: string): Promise<any | null> {
    return AdministrationFonctionnelleApi.GET(
        GET_ACTUAL_CONFIGURATION_ENDPOINT,
        token
    )
        .then(response => {
            return response.data;
        })
        .catch(error => {
            console.error(
                'AdministrationFonctionnelleApi?getConfiguration --> error : ',
                error
            );
            return null;
        });
}

export function updateConfiguration(
    data: any,
    token: string
): Promise<any | null> {
    return AdministrationFonctionnelleApi.PUT(
        PUT_CONFIGURATION_ENDPOINT,
        data,
        token
    )
        .then(response => {
            return response.data;
        })
        .catch(error => {
            console.error(
                'AdministrationFonctionnelleApi?updateConfiguration --> error : ',
                error
            );
            return null;
        });
}
