
export interface UserSearch{
    success: boolean;
    error_message: string;
    previous: number;
    next: number;
    count: number;
    users: UserSearchUser[];
}

export interface UserSearchUser{
    user_id: string;
    name: string;
    username: string;
    bio: string;
    photo_url: string;
}