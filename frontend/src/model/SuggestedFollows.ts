export interface SuggestedFollows {
    previous: number;
    next: number;
    count: number;
    success: boolean;
    error_message: string;
    users: SuggestedFollowsInner[];
}

export interface SuggestedFollowsInner {
    user_id: string;
    name: string;
    photo_url: string;
    username: string;
    bio: string;
}