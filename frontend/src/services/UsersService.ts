import axios from 'axios';
import {UserProfileResponse} from "@/model/UserProfile";
import {SuggestedFollows} from "@/model/SuggestedFollows";
import {MeUserProfileResponse} from "@/model/MeResponse";
import {EmptyResponse} from "@/model/EmptyResponse";
import {Followers} from "@/model/Followers";
import {Following} from "@/model/Following";
import {UserSearch} from "@/model/UserSearch";
import {Notifications} from "@/model/Notifications";

const API_URL = 'http://localhost:8080/api/';

class UsersService {
    getUser(userId: string) {
        return axios
            .post<UserProfileResponse>(API_URL + 'get_profile', {'user_id': Number(userId)})
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant get /get_profile, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    getSuggestedFollow(pageSize: number, page: number) {
        return axios
            .get<SuggestedFollows>(API_URL + `get_suggested_follows_all?page_size=${pageSize}&page=${page}`)
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant get /get_suggested_follows_all, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    me() {
        let timeZone = 'Asia/Tokyo';
        try {
            timeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
        } catch (e) {
            // do nothing
        }
        return axios
            .post<MeUserProfileResponse>(API_URL + 'me', {'timezone_identifier': timeZone})
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant get /me, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    follow(user_id: number) {
        return axios
            .post<EmptyResponse>(API_URL + 'follow', {'user_id': user_id})
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant get /follow, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    unfollow(user_id: number) {
        return axios
            .post<EmptyResponse>(API_URL + 'unfollow', {'user_id': user_id})
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant get /unfollow, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    searchUser(query: string, cofollows_only: boolean, following_only: boolean, followers_only: boolean, page_size = 50, page = 1) {
        return axios
            .post<UserSearch>(API_URL + 'search_users', {
                'query': query, 'cofollows_only': cofollows_only,
                'following_only': following_only, 'followers_only': followers_only,
                'page_size': page_size, 'page': page
            })
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant get /search_users, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    getFollowers(user_id: string, pageSize: number, page: number) {
        return axios
            .get<Followers>(API_URL + `get_followers?user_id=${user_id}&page_size=${pageSize}&page=${page}`)
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant get /get_followers, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    getFollowing(user_id: string, pageSize: number, page: number) {
        return axios
            .get<SuggestedFollows>(API_URL + `get_following?user_id=${user_id}&page_size=${pageSize}&page=${page}`)
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant get /get_following, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    getNotifications(pageSize: number, page: number) {
        return axios
            .get<Notifications>(API_URL + `get_notifications?page_size=${pageSize}&page=${page}`)
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant get /get_notifications, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    updateBio(bio: string) {
        return axios
            .post<EmptyResponse>(API_URL + "update_bio", {"bio": bio})
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant update bio, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }
            });
    }

    updateName(name: string) {
        return axios
            .post<EmptyResponse>(API_URL + "update_name", {"name": name})
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant update name, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }
            });
    }

    updateUsername(username: string) {
        return axios
            .post<EmptyResponse>(API_URL + "update_username", {"username": username})
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant update username, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }
            });
    }
}

export default new UsersService();