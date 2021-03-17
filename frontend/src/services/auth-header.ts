import {AxiosError, AxiosRequestConfig, AxiosResponse} from "axios";
import Index from '../store/index';

export function authHeader(config: AxiosRequestConfig) {
    const storedUser = localStorage.getItem('user');
    if (!storedUser) {
        return config;
    }
    let user = JSON.parse(storedUser);
    if (user && user.token && !("Authorization" in config.headers)) {
        config.headers.Authorization = 'Bearer ' + user.token;
    }
    return config;
}

export function refreshHeader(response: AxiosResponse) {
    if (response.status === 401 || response.status === 403) {
        return Index.dispatch("Auth/signOut").then(x => response);
    }
    if ("refreshtoken" in response.headers) {
        return Index.dispatch("Auth/refreshTokenInfo", response.headers["refreshtoken"]).then(x => response);
    } else {
        return response;
    }
}

export function responseError(error: AxiosError) {
    if (!error.response) {
        return Promise.reject(error);
    }
    if (error.response.status === 401 || error.response.status === 403) {
        return Index.dispatch("Auth/signOut").then(x => {
            if (error.response && error.response.data) {
                error.message = error.message + ": " + JSON.stringify(error.response.data);
            }
            return Promise.reject(error);
        });
    }
    if ("refreshtoken" in error.response.headers) {
        return Index.dispatch("Auth/refreshTokenInfo", error.response.headers["refreshtoken"]).then(x => Promise.reject(error));
    } else {
        if (error.response && error.response.data) {
            error.message = error.message + ": " + JSON.stringify(error.response.data);
        }
        return Promise.reject(error);
    }
}