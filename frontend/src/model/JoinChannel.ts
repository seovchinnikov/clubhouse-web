import {GetChannel, GetChannelUserProfile} from "@/model/GetChannel";

export interface JoinChannel extends GetChannel {

    token: string;
    rtm_token: string;
    pubnub_token: string;
    pubnub_origin: string;
    pubnub_heartbeat_value: number;
    pubnub_heartbeat_interval: number;
    pubnub_enable: boolean;
    agora_native_mute: boolean;
    pubnub_pub_key: string;
    pubnub_sub_key: string;
    agora_app_id: string;
    local_ws_agora_url: string;
    users: JoinChannelUserProfile[];

    success: boolean;
    error_message: string;

}


export interface JoinChannelUserProfile extends GetChannelUserProfile {

}
