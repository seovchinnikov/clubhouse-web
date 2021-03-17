import {Action, Module, Mutation, VuexModule} from 'vuex-module-decorators';
import AuthService from '@/services/AuthService';
import {UserRole} from "@/model/UserRole";
import {UserData, UserDataVolatileInfo} from "@/model/UserData";
import UsersService from "@/services/UsersService";
import {EmptyResponse} from "@/model/EmptyResponse";

const storedUser = localStorage.getItem('user');
const storedUserVolatileInfo = localStorage.getItem('storedUserVolatileInfo');

@Module({namespaced: true})
class User extends VuexModule {
    public status = storedUser ? {loggedIn: true} : {loggedIn: false};
    public user = storedUser ? JSON.parse(storedUser) : null;
    public userVolatileInfo: UserDataVolatileInfo | null = storedUserVolatileInfo ? JSON.parse(storedUserVolatileInfo) : null;

    @Mutation
    public finishPhoneAuthSuccess(user: UserData): void {
        this.status.loggedIn = true;
        this.user = user;
        this.userVolatileInfo = {
            name: user.name, photo_url: user.photo_url, username: user.username,
            updatedTime: new Date(), following_ids: [String(user.user_id)], blocked_ids: []
        };
    }

    @Mutation
    public volatileUserInfoUpdate(user: UserDataVolatileInfo): void {
        this.userVolatileInfo = user;
    }

    @Mutation
    public addFollower(user_id: number): void {
        const index = this.userVolatileInfo!.following_ids?.indexOf(String(user_id));
        if (!index || index < 0) {
            this.userVolatileInfo!.following_ids?.push(String(user_id));
        }
    }

    @Mutation
    public removeFollower(user_id: number): void {
        const index = this.userVolatileInfo!.following_ids?.indexOf(String(user_id));
        if (index && index > -1) {
            this.userVolatileInfo!.following_ids?.splice(index, 1);
        }
    }

    @Mutation
    public loginFailure(): void {
        this.status.loggedIn = false;
        this.user = null;
        this.userVolatileInfo = null;
    }

    @Mutation
    public logout(): void {
        this.status.loggedIn = false;
        this.user = null;
        this.userVolatileInfo = null;
    }


    @Action({rawError: true})
    startPhoneAuth(phone: string): Promise<any> {
        return AuthService.phoneAuthStart(phone).then(
            user => {
                // this.context.commit('phoneAuthStartSuccess', user);
                return Promise.resolve(user);
            },
            error => {
                this.context.commit('loginFailure');
                return Promise.reject(error);
            }
        );
    }

    @Action({rawError: true})
    refreshTokenInfo(newToken: string): Promise<any> {
        return AuthService.userInfo(newToken).then(
            userInfo => {
                this.context.commit('finishPhoneAuthSuccess', userInfo);
                return Promise.resolve(userInfo);
            },
            error => {
                AuthService.logout();
                this.context.commit('loginFailure');
                return Promise.reject(error);
            }
        )
    }

    @Action({rawError: true})
    finishPhoneAuth(data: any): Promise<any> {
        return AuthService.phoneAuthComplete(data.phone, data.cookie, data.verification_code).then(
            user => {
                return AuthService.userInfo(user.token).
                    // not too fast
                    then(value => new Promise(r => setTimeout(() => r(value), 1000))).then(
                        userInfo => {
                            this.context.commit('finishPhoneAuthSuccess', userInfo);
                            return Promise.resolve(userInfo);
                        },
                        error => {
                            AuthService.logout();
                            this.context.commit('loginFailure');
                            return Promise.reject(error);
                        }
                    )

            },
            error => {
                this.context.commit('loginFailure');
                return Promise.reject(error);
            }
        );
    }

    @Action({rawError: true})
    checkWaitList(): Promise<any> {
        return AuthService.checkWaitlist().then(
            userInfo => {
                this.context.commit('finishPhoneAuthSuccess', userInfo.user_body);
                return Promise.resolve(userInfo);
            },
            error => {
                // this.context.commit('loginFailure');
                return Promise.reject(error);
            }
        );
    }

    @Action({rawError: true})
    updateVolatileUserInfo(): Promise<UserDataVolatileInfo> {
        return UsersService.me().then(
            userInfo => {
                const userDataVolatileInfo: UserDataVolatileInfo = {
                    username: userInfo.user_profile!.username,
                    photo_url: userInfo.user_profile!.photo_url,
                    name: userInfo.user_profile!.name,
                    blocked_ids: userInfo.blocked_ids || [],
                    following_ids: userInfo.following_ids || [String(userInfo.user_profile!.user_id)],
                    updatedTime: new Date()
                };
                localStorage.setItem('storedUserVolatileInfo', JSON.stringify(userDataVolatileInfo));
                this.context.commit('volatileUserInfoUpdate', userDataVolatileInfo);
                return Promise.resolve(userDataVolatileInfo);
            },
            error => {
                return Promise.reject(error);
            }
        );
    }

    @Action({rawError: true})
    follow(user_id: number): Promise<EmptyResponse> {
        return UsersService.follow(user_id).then(response => {
            if (response && response.success) {
                this.context.commit('addFollower', user_id);
                localStorage.setItem('storedUserVolatileInfo', JSON.stringify(this.userVolatileInfo));
            }
            return response;
        });
    }

    @Action({rawError: true})
    unfollow(user_id: number): Promise<EmptyResponse> {
        return UsersService.unfollow(user_id).then(response => {
            if (response && response.success) {
                this.context.commit('removeFollower', user_id);
                localStorage.setItem('storedUserVolatileInfo', JSON.stringify(this.userVolatileInfo));
            }
            return response;
        });
    }

    @Action
    signOut(): void {
        AuthService.logout();
        localStorage.removeItem('storedUserVolatileInfo');
        this.context.commit('logout');
    }

    get isFollower(): (user_id: number) => boolean {
        return (user_id: number) => {
            if (!this.userVolatileInfo?.following_ids) {
                return false;
            }
            return this.userVolatileInfo?.following_ids.includes(String(user_id));
        }
    }

    get isLoggedIn(): boolean {
        return this.status.loggedIn;
    }

    get isActive(): boolean {
        return this.status.loggedIn && this.user.roles.includes(UserRole.ROLE_ACTIVE);
    }

    get isNoWait(): boolean {
        return this.status.loggedIn && this.user.roles.includes(UserRole.ROLE_NOWAIT);
    }

    get isBoarded(): boolean {
        return this.status.loggedIn && this.user.roles.includes(UserRole.ROLE_BOARDED);
    }
}

export default User;