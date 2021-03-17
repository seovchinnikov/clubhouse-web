import axios from 'axios';
import {GetClub} from "@/model/GetClub";

const API_URL = 'http://localhost:8080/api/';

class ClubsService {
    getClub(club_id: number) {
        return axios
            .post<GetClub>(API_URL + 'get_club', {'club_id': club_id})
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant get club, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }
}

export default new ClubsService();