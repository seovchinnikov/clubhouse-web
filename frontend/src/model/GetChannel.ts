export interface GetChannel {

    channel_id: string;
    channel: string;
    topic: string;
    is_private: boolean;
    is_social_mode: boolean;
    // club: string;
    club_name: string;
    club_id: string;
    is_handraise_enabled: boolean;
    handraise_permission: number;

    users: GetChannelUserProfile[];

    success: boolean;
    error_message: string;

}


export interface GetChannelUserProfile {
    user_id: string;
    name: string;
    username: string;
    bio: string;
    photo_url: string;
    is_speaker: boolean;
    is_muted: boolean;
    is_speaking: boolean;
    is_moderator: boolean;
    is_invited_as_speaker: boolean;
    raise_hands: boolean;
}
