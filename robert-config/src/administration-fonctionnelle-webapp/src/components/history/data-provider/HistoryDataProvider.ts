import AdministrationFonctionnelleApi from '../../../toolbox/administration-fonctionnelle-api';
import { GET_HISTORY_ENDPOINT } from '../../../constantes/Environments';

export type HistoryDataProviderResponse = {
    date: Date;
    message: string;
};

export function getHistory(
    token: string
): Promise<HistoryDataProviderResponse[] | null> {
    return AdministrationFonctionnelleApi.GET(GET_HISTORY_ENDPOINT, token)
        .then(response => {
            return response.data as HistoryDataProviderResponse[];
        })
        .catch(error => {
            console.error(
                'AdministrationFonctionnelleApi?getHistory --> error : ',
                error
            );
            return null;
        });
}
