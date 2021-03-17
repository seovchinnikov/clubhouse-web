export interface UserData {
    user_id: string;
    user_token: string;
    user_device: string;
    user_cookie: string;

    username: string;
    name: string;
    photo_url: string;

    roles: String[];

    token: string;

    success: boolean;
    error_message: string;
}


export interface UserDataVolatileInfo {
    username?: string;
    name?: string;
    photo_url?: string;

    following_ids?: string[];
    blocked_ids?: string[];
    updatedTime: Date;
}