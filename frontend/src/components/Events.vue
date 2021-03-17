<template>

    <v-card>
        <v-card-title>
            Events
        </v-card-title>
        <v-container v-if="events && events.length">

            <v-row>
                <v-col cols="4" v-for="event in events">

                    <v-card :color="event.channel?'blue lighten-4':'white'"
                            :loading="loading"
                            class="mx-auto"
                            max-width="550"

                    >
                        <v-container>
                            <v-card-title @click="openChannel(event, event.channel)"
                                          :class="event.channel ? 'linked_element' : ''">
                                <v-clamp autoresize :max-lines="3">{{event.name}}</v-clamp>
                            </v-card-title>
                            <v-card-text>
                                <div class="subtitle-2">
                                    {{dateFormatDefault(event.time_start)}}
                                </div>
                                <div>
                                    <span v-for="user in event.hosts.slice(0, 10)" :key="user.user_id">
                                        <v-tooltip bottom>
                                            <template #activator="{ on }">
                                                <v-avatar size="38" @click="openProfile(user.user_id)" class="linked_element"  v-on="on">
                                                    <v-img
                                                            :src="user.photo_url ? user.photo_url: '/img/no-avatar.png'"
                                                            :alt="user.name"
                                                    ></v-img>
                                                 </v-avatar>
                                            </template>
                                        <span>{{user.name}}</span>
                                        </v-tooltip>
                                    </span>
                                </div>
                                <div>
                                    <v-clamp autoresize :max-lines="7">{{event.description}}</v-clamp>
                                </div>

                                <v-divider class="mx-4"></v-divider>

                                <v-row no-gutters>
                                    <v-col>
                                            <span>
                                                <v-btn color="primary" :disabled="!event.channel"
                                                       @click="openChannel(event, event.channel)"
                                                       class="ma-2 white--text linked_element">
                                                        <v-icon large left
                                                                color="blue lighten-1">arrow_circle_up</v-icon>
                                                        <span>Join!</span>
                                                </v-btn>
                                            </span>
                                    </v-col>
                                </v-row>
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
    import {namespace} from "vuex-class";
    import ChannelsService from "@/services/ChannelsService";
    import {GetEventEventResponse} from "@/model/GetEvents";
    import dateFormat from 'dateformat';
    import VClamp from 'vue-clamp';

    const Auth = namespace("Auth");
    @Component({components: {VClamp}})
    export default class Events extends Vue {
        @Auth.Getter
        private isLoggedIn!: boolean;

        @Auth.Getter
        private isActive!: boolean;

        private loading: boolean = false;
        private message: string = "";
        private events: GetEventEventResponse[] = [];
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
            ChannelsService.getEvents(50, this.pageNumber).then(data => {
                    Array.prototype.push.apply(this.events, data.events);
                    this.hasNext = !!data.next;
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

        dateFormatDefault(date: string) {
            return dateFormat(date, "default");
        }

        protected openChannel(event: GetEventEventResponse, channel: string | null | undefined) {
            if (!channel) {
                return;
            }
            if (event.is_member_only && event.club_is_member <= 0) {
                this.$snackbar("This event is for club's members only, sorry!", 'info');
                return;
            }
            this.$confirm("Do you want to join this channel?").then(res => {
                if (res) {
                    this.$router.push({name: 'JoinChannel', params: {'channelName': "" + channel}});
                }
            });
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

    .linked_element {
        cursor: pointer !important;
    }

</style>