

/**
 * base url of the api the webapp should load the data from.
 */
export const  API_BASE_URL : string = process.env.REACT_APP_API_BASE_URL ? process.env.REACT_APP_API_BASE_URL : "http://localhost:8888/api/v1/config" ;

/**
 * target profile to be administrated
 */
export const  PROFILE : string = process.env.REACT_APP_PROFILE ? process.env.REACT_APP_PROFILE : "dev" ;

/**
 * history endpoint default is /history/{PROFILE}
 */
export const  GET_HISTORY_ENDPOINT : string = process.env.REACT_APP_HISTORY_ENDPOINT ? process.env.REACT_APP_HISTORY_ENDPOINT : "/history/"+PROFILE;

/**
 * get actual configuration /{PROFILE}
 */
export const GET_ACTUAL_CONFIGURATION_ENDPOINT : string = process.env.REACT_APP_ACTUAL_CONFIGURATION_ENDPOINT ? process.env.REACT_APP_ACTUAL_CONFIGURATION_ENDPOINT : "/"+ PROFILE;

/**
 * put configuration updates /{PROFILE}
 */
export const PUT_CONFIGURATION_ENDPOINT : string = process.env.REACT_APP_PUT_CONFIGURATION_ENDPOINT ? process.env.REACT_APP_PUT_CONFIGURATION_ENDPOINT : "/"+ PROFILE;

