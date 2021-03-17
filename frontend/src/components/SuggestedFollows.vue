<template>

    <v-card>
        <v-card-title>
            Recommended follows
        </v-card-title>
        <v-container v-if="userData && userData.length">

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

                                <div>{{user.bio}}</div>

                            </v-card-text>

                        </v-container>


                    </v-card>
                </v-col>
            </v-row>
        </v-container>
        <v-layout justify-center class="py-5 px-5">
            <v-btn @click="onNextPage" :disabled="!this.hasNext || this.loading"
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
    import {Component, Vue, Watch} from "vue-property-decorator";
    import UsersService from "@/services/UsersService";
    import {namespace} from "vuex-class";
    import {SuggestedFollowsInner} from "@/model/SuggestedFollows";

    const Auth = namespace("Auth");
    @Component
    export default class UserProfile extends Vue {
        @Auth.Getter
        private isLoggedIn!: boolean;

        @Auth.Getter
        private isActive!: boolean;

        private loading: boolean = false;
        private message: string = "";
        private userData: SuggestedFollowsInner[] = [];
        private pageNumber: number = 1;
        private hasNext: boolean = false;

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
            UsersService.getSuggestedFollow(50, this.pageNumber).then(data => {
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