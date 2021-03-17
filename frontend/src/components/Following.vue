<template>

    <v-card>
        <v-card-title>
           Following ({{count}})
        </v-card-title>


        <v-container>

            <v-row>
                <v-col cols="3" v-for="user in userData">
                    <v-card @click="openProfile(user.user_id)"
                            class="mx-auto linked"
                            max-width="450"

                    >
                        <v-img
                                height="200"
                                :src="user.photo_url ? user.photo_url: '/img/no-avatar.png'"
                        ></v-img>
                        <v-container>
                            <v-card-title>{{user.name}}</v-card-title>
                            <v-card-text>
                                <div class="subtitle-2">
                                    @{{user.username}}
                                </div>

                                <div>
                                    <v-clamp autoresize :max-lines="5">{{user.bio}}</v-clamp>
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

    const Auth = namespace("Auth");
    @Component
    export default class Following extends Vue {
        @Auth.Getter
        private isLoggedIn!: boolean;

        @Auth.Getter
        private isActive!: boolean;

        private loading: boolean = false;
        private message: string = "";
        private userData: UserSearchUser[] = [];
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
            UsersService.getFollowing(this.id, 50, this.pageNumber).then(data => {
                    Array.prototype.push.apply(this.userData, data.users);
                    this.hasNext = !!data.next;
                    // this.userData = data.users;
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