<template>

    <v-card
            class="text-center"
            max-width="110" max-height="190" min-width="70"
            :color="user.is_speaking?'blue lighten-4':'white'"

    >
        <v-flex>
            <!--            <v-img-->
            <!--                    height="60"-->
            <!--                    :src="user.photo_url ? user.photo_url: '/img/no-avatar.png'"-->
            <!--            ></v-img>-->
            <v-badge
                    avatar
                    bordered
                    left
                    overlap offset-x="35" offset-y="20"
            >
                <template v-slot:badge>
                    <!--                    <v-icon v-if="user.is_speaker">volume_up</v-icon>-->
                    <v-icon size="23" v-if="!user.is_muted">volume_up</v-icon>
                    <v-icon size="23" v-if="user.is_muted">volume_off</v-icon>
                    <v-icon size="23" v-if="user.is_moderator">build_circle</v-icon>
                </template>
                <v-avatar size="70" @click="openProfile(user.user_id)" class="linked">
                    <v-img :src="user.photo_url ? user.photo_url: '/img/no-avatar.png'"
                    ></v-img>
                </v-avatar>
            </v-badge>
            <p class="smallmarginb subheading mt-1 text-xs-center">
                {{user.name}}
            </p>
            <div class="text-xs-center">
                <v-menu
                        bottom
                        center
                >
                    <template v-slot:activator="{ on, attrs }">
                        <v-btn
                                dark
                                icon
                                v-bind="attrs"
                                v-on="on"
                        >
                            <v-icon large color="blue darken-2">mdi-dots-horizontal</v-icon>
                        </v-btn>
                    </template>

                    <v-list>
                        <v-list-item @click="openProfile(user.user_id)"
                        >
                            <v-list-item-title>Open profile</v-list-item-title>
                        </v-list-item>


                        <v-list-item v-if="moderator && user.is_speaker" @click="uninvite(user.user_id)"
                        >
                            <v-list-item-title>Remove speaker role</v-list-item-title>
                        </v-list-item>


                        <v-list-item v-if="moderator && !user.is_speaker" @click="invite(user.user_id)"
                        >
                            <v-list-item-title>Invite as speaker</v-list-item-title>
                        </v-list-item>


                        <v-list-item v-if="moderator && !user.is_moderator" @click="makeModerator(user.user_id)"
                        >
                            <v-list-item-title>Make moderator</v-list-item-title>
                        </v-list-item>


                        <v-list-item v-if="moderator" @click="blockFromChannel(user.user_id)"
                        >
                            <v-list-item-title>Block from channel</v-list-item-title>
                        </v-list-item>
                    </v-list>
                </v-menu>
            </div>
        </v-flex>
    </v-card>

</template>

<script lang="ts">
    import {Component, Prop, Vue} from "vue-property-decorator";
    import {GetChannelUserProfile} from "@/model/GetChannel";
    import ChannelsService from "@/services/ChannelsService";

    @Component
    export default class UserAvatar extends Vue {
        @Prop({required: true})
        user!: GetChannelUserProfile;

        @Prop({type: Boolean, required: true})
        moderator: boolean = false;

        @Prop({type: String, required: true})
        channel: string = "";

        protected openProfile(id: string) {
            let route = this.$router.resolve({name: 'Profile', params: {'id': "" + id}});
            window.open(route.href, '_blank');
        }

        protected invite(user_id: string) {
            ChannelsService.inviteSpeaker(this.channel, Number(user_id)).then(data => {
                if (!data.success) {
                    console.error(`Your try to invite ${user_id} was not successful`);
                    return Promise.reject(`Your try to invite ${user_id}  was not successful`);
                } else {
                    // this.user.is_speaker = true;
                    return data;
                }
            }).catch(reason => {
                this.$snackbar(`Your try to invite ${user_id} was not successful`, 'info');
                console.error(`Your try to invite ${user_id} was not successful`, reason);
            })
        }

        protected uninvite(user_id: string) {
            ChannelsService.unInviteSpeaker(this.channel, Number(user_id)).then(data => {
                if (!data.success) {
                    console.error(`Your try to uninvite ${user_id} was not successful`);
                    return Promise.reject(`Your try to uninvite ${user_id}  was not successful`);
                } else {
                    this.user.is_speaker = false;
                    return data;
                }
            }).catch(reason => {
                this.$snackbar(`Your try to uninvite ${user_id} was not successful`, 'info');
                console.error(`Your try to uninvite ${user_id} was not successful`, reason);
            })
        }

        protected makeModerator(user_id: string) {
            ChannelsService.makeModerator(this.channel, Number(user_id)).then(data => {
                if (!data.success) {
                    console.error(`Your try to makeModerator ${user_id} was not successful`);
                    return Promise.reject(`Your try to makeModerator ${user_id}  was not successful`);
                } else {
                    this.user.is_moderator = true;
                    return data;
                }
            }).catch(reason => {
                this.$snackbar(`Your try to makeModerator ${user_id} was not successful`, 'info');
                console.error(`Your try to makeModerator ${user_id} was not successful`, reason);
            })
        }

        protected blockFromChannel(user_id: string) {
            ChannelsService.blockFromChannel(this.channel, Number(user_id)).then(data => {
                if (!data.success) {
                    console.error(`Your try to blockFromChannel ${user_id} was not successful`);
                    return Promise.reject(`Your try to blockFromChannel ${user_id}  was not successful`);
                } else {
                    return data;
                }
            }).catch(reason => {
                this.$snackbar(`Your try to blockFromChannel ${user_id} was not successful`, 'info');
                console.error(`Your try to blockFromChannel ${user_id} was not successful`, reason);
            })
        }
    }
</script>

<style scoped>
    a.v-btn:hover {
        text-decoration: none !important;
    }

    .my-12 {
        cursor: pointer !important;
    }

    .linked {
        cursor: pointer !important;
    }

    .smallmarginb {
        margin-bottom: 5px !important;
    }
</style>