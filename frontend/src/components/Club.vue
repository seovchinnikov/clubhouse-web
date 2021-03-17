<template>
    <v-card
            :loading="loading"
            class="mx-auto my-12"
            max-width="574"
    >
        <v-card-title>Club info</v-card-title>

        <v-img
                height="250"
                :src="avatar"
        ></v-img>

<!--        <v-btn class="ma-2 white&#45;&#45;text" color="blue"-->
<!--               v-if="this.id !== String(currentUser.user_id) && !this.isFollower(Number(this.id))"-->
<!--               @click="() => this.follow(Number(this.id))" dark large absolute top right fab>-->
<!--            <v-icon>person_add</v-icon>-->
<!--        </v-btn>-->
<!--        <v-btn class="ma-2 white&#45;&#45;text" color="blue"-->
<!--               v-if="this.id !== String(currentUser.user_id) && this.isFollower(Number(this.id))"-->
<!--               @click="() => this.unfollow(Number(this.id))"-->
<!--               dark large absolute top right fab>-->
<!--            <v-icon>person_remove</v-icon>-->
<!--        </v-btn>-->

        <v-container>
            <v-row no-gutters>
                <v-col cols="7">
                    <v-card-title>{{clubData ? clubData.club.name : ""}}</v-card-title>

                    <v-card-text>
<!--                        <div class="my-4 subtitle-1">-->
<!--                            @{{clubData ? clubData.club.username : ""}}-->
<!--                        </div>-->

                        <div>{{clubData ? clubData.club.description : ""}}</div>

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
                            <span class="font-weight-black">{{clubData ? clubData.club.num_followers : ""}}</span> followers
                        </div>

                        <div class="my-4 subtitle-1"><span class="font-weight-black">{{clubData ? clubData.club.num_members : ""}}</span>
                            members
                        </div>

                    </v-card-text>
                </v-col>
            </v-row>
        </v-container>
        <v-divider class="mx-4"></v-divider>


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
    import {GetClub} from "@/model/GetClub";
    import ClubsService from "@/services/ClubsService";

    const Auth = namespace("Auth");
    @Component
    export default class Club extends Vue {
        @Auth.Getter
        private isLoggedIn!: boolean;

        @Auth.Getter
        private isActive!: boolean;

        // @Auth.Getter
        // private isFollower!: (user_id: number) => boolean;
        //
        // @Auth.Action
        // private follow!: (user_id: number) => Promise<EmptyResponse>;

        // @Auth.Action
        // private unfollow!: (user_id: number) => Promise<EmptyResponse>;
        @Auth.State("user")
        private currentUser!: any;

        private loading: boolean = false;
        private message: string = "";
        private clubData!: GetClub;
        private avatar: string = '/img/no-avatar.png';
        @Prop({type: String, required: true})
        id!: string;

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
            ClubsService.getClub(Number(this.id)).then(data => {
                    this.clubData = data;
                    this.loading = false;
                    this.avatar = this.clubData.club.photo_url;
                },
                (error) => {
                    this.loading = false;
                    this.message = error;
                });
        }


    }
</script>

<style scoped>
    a.v-btn:hover {
        text-decoration: none !important;
    }

</style>