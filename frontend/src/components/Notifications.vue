<template>

    <v-card>
        <v-card-title>
            Notifications ({{count}})
        </v-card-title>


        <v-container>

            <v-row>
                <v-col cols="3" v-for="notif in notifications">
                    <v-card @click="openProfile(notif.user_profile ? notif.user_profile.user_id : '')"
                            class="mx-auto linked"
                            max-width="450"

                    >
                        <v-img
                                height="200"
                                :src="notif.user_profile && notif.user_profile.photo_url ? notif.user_profile.photo_url: '/img/no-avatar.png'"
                        ></v-img>
                        <v-container>
                            <v-card-title>{{notif.user_profile ? notif.user_profile.name : ""}}</v-card-title>
                            <v-card-text>
                                <div class="subtitle-2">
                                    {{dateFormatDefault(notif.time_created)}}
                                </div>

                                <div>
                                    <v-clamp autoresize :max-lines="5">{{notif.message}}</v-clamp>
                                </div>

                            </v-card-text>

                        </v-container>
                    </v-card>
                </v-col>
            </v-row>
        </v-container>

        <v-layout justify-center class="py-5 px-5">
            <v-btn @click="onNextPage" :disabled="!hasNext || loading"
                   depressed
                   color="primary"
            >
                Load more
            </v-btn>
        </v-layout>
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
    import {namespace} from "vuex-class";
    import {UserSearchUser} from "@/model/UserSearch";
    import {Notifications} from "../model/Notifications";
    import dateFormat from "dateformat";

    const Auth = namespace("Auth");
    @Component
    export default class Following extends Vue {
        @Auth.Getter
        private isLoggedIn!: boolean;

        @Auth.Getter
        private isActive!: boolean;

        private loading: boolean = false;
        private message: string = "";
        private notifications: Notifications[] = [];
        private pageNumber: number = 1;
        private hasNext: boolean = false;
        private count: number = 0;

        @Prop({type: String, required: true})
        id!: string;


        mounted() {
            if (!this.isActive) {
                this.$router.push("/");
                return;
            }
            this.getDataFromApi();
        }

        @Watch('loading')
        onLoadingChanged(value: string, oldValue: string) {
            this.$emit("loadingevent", value);
        }

        getDataFromApi() {
            this.loading = true;
            UsersService.getNotifications(50, this.pageNumber).then(data => {
                    Array.prototype.push.apply(this.notifications, data.notifications);
                    this.hasNext = !!data.next;
                    this.loading = false;
                    this.count = data.count;
                },
                (error) => {
                    this.loading = false;
                    this.message = error;
                });

        }

        openProfile(id: string) {
            this.$router.push({name: 'Profile', params: {'id': "" + id}})
        }

        onNextPage() {
            this.pageNumber += 1;
            this.getDataFromApi();
        }

        dateFormatDefault(date: string) {
            return dateFormat(date, "default");
        }
    }
</script>

<style scoped>
    a.v-btn:hover {
        text-decoration: none !important;
    }

    .linked {
        cursor: pointer !important;
    }

</style>