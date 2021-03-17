<template>

    <v-card>
        <v-card-title>
            Search
        </v-card-title>

        <v-row>
            <v-col cols="1">
            </v-col>
            <v-col cols="3">
                <v-text-field
                        label="Search query"
                        placeholder="Search query"
                        v-model="query"
                ></v-text-field>
            </v-col>
            <v-col cols="2">
                <v-checkbox
                        v-model="cofollowsOnly"
                        label="Cofollows only"
                ></v-checkbox>
            </v-col>
            <v-col cols="2">
                <v-checkbox
                        v-model="followersOnly"
                        label="Followers only"
                ></v-checkbox>
            </v-col>
            <v-col cols="2">
                <v-checkbox
                        v-model="followingOnly"
                        label="Following only"
                ></v-checkbox>
            </v-col>
            <v-col cols="2">
                <div>
                    <v-btn color="primary" :disabled="!this.query || this.loading"
                           @click="onSearch"
                           class="ma-2 white--text linked_element">
                        <v-icon large left
                                color="blue lighten-1">search
                        </v-icon>
                        <span>Search</span>
                    </v-btn>
                </div>
            </v-col>
        </v-row>

        <v-tabs
                v-model="tab"
        >
            <v-tab>Users</v-tab>
            <v-tab-item
            >
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

                                        <div><v-clamp autoresize :max-lines="5">{{user.bio}}</v-clamp></div>

                                    </v-card-text>

                                </v-container>


                            </v-card>
                        </v-col>
                    </v-row>
                </v-container>
            </v-tab-item>
        </v-tabs>
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
    export default class UserProfile extends Vue {
        @Auth.Getter
        private isLoggedIn!: boolean;

        @Auth.Getter
        private isActive!: boolean;

        private loading: boolean = false;
        private message: string = "";
        private userData: UserSearchUser[] = [];
        private pageNumber: number = 1;
        private hasNext: boolean = false;
        private query: string = "";
        private cofollowsOnly: boolean = false;
        private followingOnly: boolean = false;
        private followersOnly: boolean = false;

        private tab: string = '0';

        mounted() {
            if (!this.isActive) {
                this.$router.push("/");
                return;
            }
        }

        @Watch('loading')
        onLoadingChanged(value: string, oldValue: string) {
            this.$emit("loadingevent", value);
        }

        onSearch() {
            this.userData.splice(0, this.userData.length);
            this.pageNumber = 1;
            this.hasNext = false;
            this.getDataFromApi();
        }

        @Watch('tab')
        onTabChanged(value: string, oldValue: string) {
            this.userData.splice(0, this.userData.length);
            this.pageNumber = 1;
            this.hasNext = false;
        }

        getDataFromApi() {
            if (String(this.tab) == '0' && this.query) {
                this.loading = true;
                UsersService.searchUser(this.query, this.cofollowsOnly, this.followingOnly,
                    this.followersOnly, 50, this.pageNumber).then(data => {
                        Array.prototype.push.apply(this.userData, data.users);
                        this.hasNext = !!data.next;
                        // this.userData = data.users;
                        this.loading = false;
                    },
                    (error) => {
                        this.loading = false;
                        this.message = error;
                    });
            }
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