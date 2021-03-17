export interface UserProfileResponse {
    user_profile: UserProfileInfoResponse;
    success: boolean;
    error_message: string;
}

export interface UserProfileInfoResponse {
    name: string;
    username: string;
    bio: string;
    twitter: string;
    instagram: string;
    num_followers: number;
    num_following: number;
    follows_me: boolean;
    mutual_follows_count: number;
    photo_url: string;
    clubs: GetEventClubResponse[];
}

export interface GetEventClubResponse {
    club_id: number;
    name: string;
    description: string;
    photo_url: string;
    num_members: number;
    num_followers: number;

}