import AdministrationFonctionnelleApi from "../../../toolbox/administration-fonctionnelle-api";
import {GET_HISTORY_ENDPOINT} from "../../../constantes/Environments";

export type HistoryDataProviderResponse = {
    date: Date,
    message: string
}

export function getHistory() : Promise< HistoryDataProviderResponse[] | null >
{
    return AdministrationFonctionnelleApi.GET(GET_HISTORY_ENDPOINT)
        .then(response => {
            console.log("AdministrationFonctionnelleApi?getHistory --> response : ", response.data as HistoryDataProviderResponse[]);
            return response.data as HistoryDataProviderResponse[];
        })
        .catch(error => {
            console.error("AdministrationFonctionnelleApi?getHistory --> error : ",error);
            return null;
        })
        .finally(() => {
            console.log("AdministrationFonctionnelleApi?getHistory --> finally : closing getHistory api call");
        });
}


