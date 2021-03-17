export interface GetEvents {
    previous: number | null;
    next: number | null;
    count: number;
    success: boolean;
    error_message: string;
    events: GetEventEventResponse[];
}

export interface GetEventEventResponse {
    event_id: number;
    name: string;
    description: string;
    time_start: string;
    club: GetEventClubResponse | null;
    is_member_only: boolean;
    url: string;
    channel: string | null;
    club_is_member: number;
    club_is_follower: number;
    is_expired: boolean;
    hosts: GetEventUserResponse[];

}

export interface GetEventClubResponse {
    club_id: number;
    name: string;
    description: string;
    photo_url: string;
    num_members: number;
    num_followers: number;

}

export interface GetEventUserResponse {
    user_id: string;
    name: string;
    photo_url: string;
    username: string;
    bio: string;

}