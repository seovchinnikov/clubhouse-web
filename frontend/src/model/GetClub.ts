export interface GetClub{
    is_admin: boolean;
    is_member: boolean;
    is_follower: boolean;
    club: GetClubInfo;

    success: boolean;
    error_message: string;

}


export interface GetClubInfo {
    club_id: string;
    name: string;
    description: string;
    photo_url: string;
    num_members: number;
    num_followers: number;
    is_follow_allowed: boolean;
    is_membership_private: boolean;
    is_community: boolean;
}
