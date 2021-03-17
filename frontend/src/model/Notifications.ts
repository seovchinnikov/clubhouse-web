export interface Notifications {
    previous: number;
    next: number;
    count: number;
    success: boolean;
    error_message: string;
    notifications: Notification[];
}

export interface Notification {
    message: string;
    type: number;
    is_unread: boolean;
    time_created: string;
    user_profile: NotificationUserProfile;
}

export interface NotificationUserProfile {
    user_id: string;
    username: string;
    name: string;
    photo_url: string;
}