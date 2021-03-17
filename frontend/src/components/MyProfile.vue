<template>
    <v-card
            :loading="loading"
            class="mx-auto my-12"
            max-width="574"
    >
        <v-img
                height="250"
                :src="avatar"
        ></v-img>


        <v-container>
            <v-row no-gutters>
                <v-col cols="7">
                    <v-card-title>{{userData ? userData.name : ""}}</v-card-title>

                    <v-card-text>
                        <div class="my-4 subtitle-1">
                            @{{userData ? userData.username : ""}}
                        </div>

                        <div>{{userData ? userData.bio : ""}}</div>

                        <div></div>
                    </v-card-text>
                </v-col>
                <v-col cols="1">
                    <v-spacer></v-spacer>
                </v-col>
                <v-col cols="4">
                    <v-card-title>Followers</v-card-title>
                    <v-card-text>
                        <div class="my-4 subtitle-1">
                            <v-btn color="primary" @click="openFollowers(currentUser.user_id)"><span
                                    class="font-weight-black">{{userData ? userData.num_followers : ""}}</span>
                                followers
                            </v-btn>
                        </div>

                        <div class="my-4 subtitle-1">
                            <v-btn color="primary" @click="openFollowing(currentUser.user_id)"><span
                                    class="font-weight-black">{{userData ? userData.num_following : ""}}</span>
                                following
                            </v-btn>
                        </div>

                        <div class="my-4 subtitle-1"><span class="font-weight-black">{{userData ? userData.mutual_follows_count : ""}}</span>
                            mutual
                        </div>
                    </v-card-text>
                </v-col>
            </v-row>
        </v-container>
        <v-divider class="mx-4"></v-divider>

        <v-container v-if="userData && userData.clubs">
            <div><span class="text-h5">Clubs</span></div>
            <div>
                <span v-for="club in userData.clubs.slice(0, 12)">
                    <v-tooltip bottom>
                            <template #activator="{ on }">
                                    <v-avatar size="38" v-on="on" @click="openClub(String(club.club_id))">
                                        <v-img
                                                :src="club.photo_url ? club.photo_url: '/img/no-avatar.png'"
                                                :alt="club.name"
                                        ></v-img>
                                     </v-avatar>
                            </template>
                        <span>{{club.name}}</span>
                    </v-tooltip>
                </span>
            </div>
        </v-container>

        <v-divider class="mx-4"></v-divider>

        <v-container>
            <v-row no-gutters>
                <v-col>
                    <span v-if="userData && userData.twitter">
                        <v-btn color="primary" class="ma-2 white--text" v-bind:id="this.name"
                               v-bind:href="'https://twitter.com/'+userData.twitter" target="_blank">
                            <v-icon large left color="blue lighten-1">mdi-twitter</v-icon>
                            <span>@{{userData.twitter}}</span>
                        </v-btn>
                    </span></v-col>
                <v-col>
                    <v-spacer></v-spacer>
                </v-col>
                <v-col><span v-if="userData && userData.instagram">
                        <v-btn color="purple accent-1" class="ma-2 white--text" v-bind:id="this.name"
                               v-bind:href="'https://instagram.com/'+userData.instagram" target="_blank">
                            <v-icon large left color="blue lighten-1">mdi-instagram</v-icon>
                            <span>{{userData.instagram}}</span>
                        </v-btn>
                    </span></v-col>
            </v-row>
        </v-container>

        <v-divider class="mx-4"></v-divider>

        <!--        <v-card-title></v-card-title>-->

        <!--        <v-card-text>-->
        <!--            -->
        <!--        </v-card-text>-->

        <v-card-actions>
            <v-btn
                    color="deep-purple lighten-2"
                    text
                    @click="getDataFromApi"
            >
                Update
            </v-btn>
        </v-card-actions>
        <div class="form-group">
            <div v-if="message" class="alert alert-danger" role="alert">
                {{ message }}
            </div>
        </div>
    </v-card>
</template>


<script lang="ts">
    import {Component, Prop, Vue, Watch} from "vue-property-decorator";
    import UsersService from "@/services/UsersService";
    import {UserProfileInfoResponse} from "@/model/UserProfile";
    import {namespace} from "vuex-class";
    import {EmptyResponse} from "@/model/EmptyResponse";

    const Auth = namespace("Auth");
    @Component
    export default class UserProfile extends Vue {
        @Auth.Getter
        private isLoggedIn!: boolean;

        @Auth.Getter
        private isActive!: boolean;

        @Auth.Getter
        private isFollower!: (user_id: number) => boolean;

        @Auth.Action
        private follow!: (user_id: number) => Promise<EmptyResponse>;

        @Auth.State("user")
        private currentUser!: any;

        @Auth.Action
        private unfollow!: (user_id: number) => Promise<EmptyResponse>;

        private loading: boolean = false;
        private message: string = "";
        private userData!: UserProfileInfoResponse;
        private avatar: string = '/img/no-avatar.png';


        mounted() {
            if (!this.isActive) {
                this.$router.push("/");
                return;
            }

            this.getDataFromApi()
        }

        @Watch('loading')
        onLoadingChanged(value: string, oldValue: string) {
            this.$emit("loadingevent", value);
        }

        getDataFromApi() {
            this.loading = true
            UsersService.getUser(this.currentUser.user_id).then(data => {
                    this.userData = data.user_profile;
                    this.loading = false;
                    this.avatar = this.userData.photo_url ? this.userData.photo_url : '/img/no-avatar.png';
                },
                (error) => {
                    this.loading = false;
                    this.message = error;
                });
        }

        openClub(id: string) {
            this.$router.push({name: 'Club', params: {'id': "" + id}})
        }

        openFollowing(id: string) {
            this.$router.push({name: 'Following', params: {'id': "" + id}})
        }

        openFollowers(id: string) {
            this.$router.push({name: 'Followers', params: {'id': "" + id}})
        }
    }
</script>

<style scoped>
    a.v-btn:hover {
        text-decoration: none !important;
    }

    .v-avatar {
        cursor: pointer !important;
    }
</style>