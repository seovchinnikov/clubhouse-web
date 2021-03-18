import axios from 'axios';
import {UserData} from "@/model/UserData";
import {AppSettings} from "@/services/AppSettings";

const API_URL = AppSettings.API_ENDPOINT;

class AuthService {
    phoneAuthStart(phone_number: string) {
        return axios
            .post(API_URL + 'start_phone_number_auth', {
                'phone_number': phone_number
            })
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                    // localStorage.setItem('user', JSON.stringify(response.data));
                } else {
                    throw new Error("Cant phoneAuthStart, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    phoneAuthComplete(phone_number: string, cookie: string, verification_code: string) {
        return axios
            .post(API_URL + 'complete_phone_number_auth', {
                'phone_number': phone_number,
                'cookie': cookie,
                'verification_code': verification_code
            })
            .then(response => {
                if (response.data && response.data.success) {
                    // localStorage.setItem('user', JSON.stringify(response.data));
                    return response.data;
                } else {
                    throw new Error("Cant phoneAuthComplete, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }
            });
    }


    userInfo(token: string) {
        return axios
            .get<UserData>(API_URL + 'user_info', {
                headers: {
                    Authorization: 'Bearer ' + token
                }
            })
            .then(response => {
                if (response.data && response.data.success) {
                    response.data.token = token;
                    localStorage.setItem('user', JSON.stringify(response.data));
                    return response.data;
                } else {
                    throw new Error("Cant get info, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }
            });
    }

    checkWaitlist() {
        return axios
            .post(API_URL + 'check_waitlist_status', {})
            .then(response => {
                if (response.data && response.data.success) {
                    response.data.user_body.token = response.data.token;
                    localStorage.setItem('user', JSON.stringify(response.data.user_body));
                    return response.data;
                } else {
                    throw new Error("Cant checkWaitlist, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    logout() {
        localStorage.removeItem('user');
    }

}

export default new AuthService();
