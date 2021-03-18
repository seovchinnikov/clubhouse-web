import axios from 'axios';
import {ChannelsResponse} from "@/model/ChannelsResponse";
import {GetChannel} from "@/model/GetChannel";
import {JoinChannel} from "@/model/JoinChannel";
import {EmptyResponse} from "@/model/EmptyResponse";
import {PingChannel} from "@/model/PingChannel";
import {SuggestedFollows} from "@/model/SuggestedFollows";
import {GetEvents} from "@/model/GetEvents";
import {AppSettings} from "@/services/AppSettings";

const API_URL = AppSettings.API_ENDPOINT;

class ChannelsService {
    getChannels() {
        return axios
            .get<ChannelsResponse>(API_URL + 'get_channels')
            .then(response => {
                if (response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant get channels, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    getChannel(channel: string) {
        return axios
            .post<GetChannel>(API_URL + 'get_channel', {
                'channel': channel
            })
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant get channel, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    pingChannel(channel: string) {
        return axios
            .post<PingChannel>(API_URL + 'active_ping', {
                'channel': channel,
                'channel_id': null
            })
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant ping channel, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    joinChannel(channel: string, attribution_source: string = "feed", attribution_details: string = "eyJpc19leHBsb3JlIjpmYWxzZSwicmFuayI6MX0=") {
        return axios
            .post<JoinChannel>(API_URL + 'join_channel', {
                'channel': channel,
                'attribution_source': attribution_source,
                'attribution_details': attribution_details
            })
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant join channel, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    leaveChannel(channel: string) {
        return axios
            .post<EmptyResponse>(API_URL + 'leave_channel', {
                'channel': channel
            })
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant leave channel, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    raiseHands(channel: string) {
        return axios
            .post<EmptyResponse>(API_URL + 'audience_reply', {
                'channel': channel,
                'raise_hands': true,
                'unraise_hands': false
            })
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant raise hand, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    unRaiseHands(channel: string) {
        return axios
            .post<EmptyResponse>(API_URL + 'audience_reply', {
                'channel': channel,
                'raise_hands': false,
                'unraise_hands': true
            })
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant unraise hand, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    acceptInvite(channel: string, user_id: number) {
        return axios
            .post<EmptyResponse>(API_URL + 'accept_speaker_invite', {
                'channel': channel,
                'user_id': user_id
            })
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant accept invite, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    rejectInvite(channel: string, user_id: number) {
        return axios
            .post<EmptyResponse>(API_URL + 'reject_speaker_invite', {
                'channel': channel,
                'user_id': user_id
            })
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant reject invite, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    getEvents(pageSize: number, page: number) {
        return axios
            .get<GetEvents>(API_URL + `get_events?page_size=${pageSize}&page=${page}`)
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant get /get_events, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    inviteSpeaker(channel: string, user_id: number) {
        return axios
            .post<EmptyResponse>(API_URL + 'invite_speaker', {
                'channel': channel,
                'user_id': user_id
            })
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant invite_speaker, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    unInviteSpeaker(channel: string, user_id: number) {
        return axios
            .post<EmptyResponse>(API_URL + 'uninvite_speaker', {
                'channel': channel,
                'user_id': user_id
            })
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant uninvite_speaker, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    makeModerator(channel: string, user_id: number) {
        return axios
            .post<EmptyResponse>(API_URL + 'make_moderator', {
                'channel': channel,
                'user_id': user_id
            })
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant make_moderator, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }

    blockFromChannel(channel: string, user_id: number) {
        return axios
            .post<EmptyResponse>(API_URL + 'block_from_channel', {
                'channel': channel,
                'user_id': user_id
            })
            .then(response => {
                if (response.data && response.data.success) {
                    return response.data;
                } else {
                    throw new Error("Cant block_from_channel, reason: " + response.status + " " +
                        (response.data ? response.data.error_message : ""));
                }

            });
    }
}

export default new ChannelsService();