export interface ChannelsResponse {
    channels: ChannelsResponseChannelResponse[];
    success: boolean;
    error_message: string;
}

export interface ChannelsResponseChannelResponse {
    channel: string;
    topic: string;
    is_private: boolean;
    url: string;
    creator_user_profile_id: string;
    num_speakers: number;
    num_all: number;
    users: ChannelsResponseUserResponse[];
}

export interface ChannelsResponseUserResponse {
    user_id: string;

    is_speaker: boolean;
    is_moderator: boolean;
    name: string;
    photo_url: string;
}