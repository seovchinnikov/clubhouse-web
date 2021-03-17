export interface MeUserProfileResponse {
    num_invites: number;
    has_unread_notifications:boolean;
    following_ids: string[];
    blocked_ids: string[];

    user_profile?: UserProfileInfoResponse;

    success: boolean;
    error_message: string;
}

export interface UserProfileInfoResponse {
    name: string;
    username: string;
    user_id: string;
    photo_url: string;
}