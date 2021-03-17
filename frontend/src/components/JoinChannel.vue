<template>
    <v-row>
        <v-col cols="10">
            <v-card>
                <v-card-title>
                    {{getChannel ? getChannel.topic : ""}}
                </v-card-title>

                <v-card-title>
                    Speakers
                </v-card-title>
                <user-avatar-list :users="speakers" :moderator="isModerator" :channel="channelName"></user-avatar-list>

                <div v-if="isModerator">
                    <v-card-title>
                        Hands raised
                    </v-card-title>
                    <user-avatar-list :users="handsRaisedUsers" :moderator="isModerator"
                                      :channel="channelName"></user-avatar-list>
                </div>

                <v-card-title>
                    Others
                </v-card-title>
                <user-avatar-list :users="nonSpeakers" :moderator="isModerator"
                                  :channel="channelName"></user-avatar-list>

                <div class="form-group">
                    <div v-if="message" class="alert alert-danger" role="alert">
                        {{ message }}
                    </div>
                </div>
            </v-card>
        </v-col>
        <v-col cols="2">
            <v-card>
                <v-card-title>
                    Control
                </v-card-title>

                <v-row class="py-5 px-5">
                    <v-col
                            cols="4"
                    >
                        <v-tooltip bottom :disabled="valid">
                            <template v-slot:activator="{ on }">
                                <div v-on="on" class="d-inline-block">
                                    <v-btn
                                            icon
                                            color="blue"
                                            :disabled="!isSpeaker || loading || isLeaving"
                                            @click="flipMute"
                                    >
                                        <v-icon size="50">{{isMuting ? 'volume_off' : 'volume_up'}}</v-icon>
                                    </v-btn>
                                </div>
                            </template>
                            <span>Mute/unmute (disabled if you are not a speaker)</span>
                        </v-tooltip>
                    </v-col>
                    <v-col
                            cols="4"
                    >
                        <v-tooltip bottom :disabled="valid">
                            <template v-slot:activator="{ on }">
                                <div v-on="on" class="d-inline-block">
                                    <v-btn
                                            icon
                                            color="blue"
                                            :disabled="loading || isLeaving"
                                            @click="leaveChannel"
                                    >
                                        <v-icon size="50">exit_to_app</v-icon>
                                    </v-btn>
                                </div>
                            </template>
                            <span>Leave the channel</span>
                        </v-tooltip>
                    </v-col>
                    <v-col
                            cols="4"
                    >
                        <v-tooltip bottom :disabled="valid">
                            <template v-slot:activator="{ on }">
                                <div v-on="on" class="d-inline-block">
                                    <v-btn
                                            icon
                                            color="blue"
                                            :disabled="isSpeaker || loading || isLeaving"
                                            @click="flipRaiseHands"
                                    >
                                        <v-icon size="50">{{isHandsRaised ? 'pan_tool' : 'do_not_touch'}}</v-icon>
                                    </v-btn>
                                </div>
                            </template>
                            <span>Raise/unrise hands (disabled if you are a speaker)</span>
                        </v-tooltip>
                    </v-col>
                </v-row>
            </v-card>
        </v-col>
    </v-row>
</template>


<script lang="ts">
    import {Component, Prop, Vue, Watch} from "vue-property-decorator";
    import {namespace} from "vuex-class";
    import {GetChannel, GetChannelUserProfile} from "@/model/GetChannel";
    import {JoinChannel} from "@/model/JoinChannel";
    import ChannelsService from "@/services/ChannelsService";
    import PubNub from 'pubnub';
    import UserAvatarList from "@/components/UserAvatarList.vue";
    import {Route} from "vue-router";
    import WebSocketAsPromised from "websocket-as-promised";

    const Auth = namespace("Auth");
    @Component({
        components: {UserAvatarList}
    })
    export default class UserProfile extends Vue {
        @Auth.Getter
        private isLoggedIn!: boolean;

        @Auth.State("user")
        private currentUser!: any;

        @Auth.Getter
        private isActive!: boolean;

        @Prop({type: String, required: true})
        channelName!: string;

        private loading: boolean = false;
        private message: string = "";
        private joinChannel: JoinChannel | null = null;
        private getChannel: GetChannel | null = null;

        private allUsers: GetChannelUserProfile[] = [];
        private pubnub: PubNub | null = null;

        private agoraWs: WebSocketAsPromised | null = null;
        private agoraWsPromises: Promise<any>[] | null = null;

        private isSpeaker: boolean = false;
        private isMuting: boolean = true;
        private isHandsRaised: boolean = false;
        private isModerator: boolean = false;
        private pingTimeout: number | null = null;
        private reloadChannelInfoTimeout: number | null = null;
        private isLeaving: boolean = false;
        private routeChanged: boolean = false;

        mounted() {
            if (!this.isActive) {
                this.$router.push("/");
                return;
            }
            this.callInitJoinChannel()
        }

        @Watch('loading')
        onLoadingChanged(value: string, oldValue: string) {
            this.$emit("loadingevent", value);
        }

        callInitJoinChannel() {
            this.loading = true
            ChannelsService.joinChannel(this.channelName).then(data => {
                this.joinChannel = data;
                this.getChannel = data;
                this.allUsers = data.users;
                this.extractMyInfoFromResponse(data);
                console.log("got join data from backend");
                this.pingTimeout = this.pingChannelRepeated();
                this.reloadChannelInfoTimeout = this.reloadChannelInfoRepeated();
                return data;
            }).then(this.connectToPubNub).then(this.connectToAgora).then(response => {
                this.loading = false;
                this.$snackbar("You have joined the channel!", 'info');
            }).catch(
                (error) => {
                    console.error("Error while connecting ", error);
                    this.leaveChannel(new Error("Error while connecting: " + error));
                    this.loading = false;
                    this.message = error;
                });
        }

        protected rejoinChannel() {
            this.loading = true
            return ChannelsService.leaveChannel(this.channelName).then((leaveChannelData) => {
                // unsubscribe now
                if (this.pubnub) {
                    try {
                        this.pubnub.unsubscribeAll();
                    } catch (e) {
                        console.error(e);
                    }
                }
                return leaveChannelData;
            }).then(value => {
                return new Promise(resolve => setTimeout(() => resolve(value), 1500));
            }).then(response => ChannelsService.joinChannel(this.channelName)).then(data => {
                this.joinChannel = data;
                this.getChannel = data;
                this.allUsers = data.users;
                this.extractMyInfoFromResponse(data);
                console.log("got join data from backend");
                return data;
            }).then(this.connectToPubNub).then(this.connectToAgora).then(response => {
                this.loading = false;
                this.$snackbar("You have rejoined the channel!", 'info');
            }).catch((error) => {
                console.error("Error while reconnecting ", error);
                this.leaveChannel(new Error("Error while reconnecting: " + error));
                this.loading = false;
                this.message = error;
            });
        }

        protected connectToPubNub(response: JoinChannel) {
            if (this.pubnub) {
                this.leavePubNub();
            }
            try {
                this.pubnub = new PubNub({
                    publishKey: response.pubnub_pub_key,
                    subscribeKey: response.pubnub_sub_key,
                    uuid: this.currentUser.user_id,
                    origin: response.pubnub_origin,
                    heartbeatInterval: response.pubnub_heartbeat_interval,
                    presenceTimeout: response.pubnub_heartbeat_value,
                    authKey: response.pubnub_token
                });
                this.pubnub.addListener({
                    message: (message) => {
                        if (!("channel" in message.message)
                            || String(this.channelName) !== String(message.message.channel) || !("action" in message.message)) {
                            return
                        }
                        console.info(message);
                        const action = message.message.action;
                        switch (action) {
                            case "invite_speaker":
                                if (!("from_user_id" in message.message)) {
                                    return
                                }
                                this.onInvitedAsSpeaker(message.message.channel, message.message.from_user_id,
                                    message.message.from_name)
                                break;
                            case "add_speaker":
                                if (!("user_profile" in message.message) || !("user_id" in message.message.user_profile)) {
                                    return
                                }
                                this.onAddSpeaker(message.message.channel, message.message.user_profile.user_id, true)
                                break;
                            case "make_moderator":
                                if (!("user_id" in message.message)) {
                                    return
                                }
                                this.onMakeModerator(message.message.channel, message.message.user_id, true)
                                break;
                            case "remove_speaker":
                                if (!("user_id" in message.message)) {
                                    return
                                }
                                this.onAddSpeaker(message.message.channel, message.message.user_id, false)
                                break;
                            case "remove_from_channel":
                                if (!("user_id" in message.message)) {
                                    return
                                }
                                this.onRemoveUser(message.message.channel, message.message.user_id)
                                break;
                            case "join_channel":
                                if (!("user_profile" in message.message)) {
                                    return
                                }
                                try {
                                    this.onUserJoined(message.message.user_profile);
                                } catch (e) {
                                    console.log("Exception during event user join_channel", e);
                                }
                                break;
                            case "leave_channel":
                                try {
                                    let user_id = -1;
                                    if ("user_id" in message.message) {
                                        user_id = message.message.user_id;
                                    } else if ("user_profile" in message.message) {
                                        user_id = message.message.user_profile.user_id;
                                    }
                                    if (user_id === -1) {
                                        return;
                                    }
                                    this.onUserLeft(user_id);
                                } catch (e) {
                                    console.log("Exception during event user leave_channel", e);
                                }
                                break;
                            case "end_channel":
                                console.log('leaving channel end_channel');
                                this.$snackbar("Channel has ended!", 'info');
                                this.leaveChannel()
                                break;
                            case "raise_hands":
                                if (!("user_profile" in message.message)) {
                                    return
                                }
                                try {
                                    this.onRaiseHands(message.message.user_profile.user_id);
                                } catch (e) {
                                    console.log("Exception during event raise_hands", e);
                                }
                                break;
                            case "unraise_hands":
                                if (!("user_id" in message.message)) {
                                    return
                                }
                                try {
                                    this.onUnRaiseHands(message.message.user_id);
                                } catch (e) {
                                    console.log("Exception during event unraise_hands", e);
                                }
                                break;
                        }

                    }
                });
                const channels = ["users." + this.currentUser.user_id,
                    "channel_user." + response.channel + "." + this.currentUser.user_id, "channel_all." + response.channel,
                ];
                if (this.isModerator) {
                    channels.push("channel_speakers." + response.channel);
                }
                this.pubnub.subscribe({
                    channels: channels
                });
                console.log("pubnub init was successful");
                return response;
            } catch (e) {
                this.message = "Error while connecting to pubnub: " + e;
                console.error("Error while connecting to pubnub: " + e);
                throw e;
            }
        }

        protected async connectToAgora(response: JoinChannel) {
            if (this.agoraWs) {
                await this.leaveAgoraAsync();
            }

            try {
                this.agoraWs = new WebSocketAsPromised(response.local_ws_agora_url, {
                    packMessage: data => JSON.stringify(data),
                    unpackMessage: data => {
                        console.log(data);
                        return JSON.parse(<string>data);
                    },
                    extractMessageData: event => event.data,
                    attachRequestId: (data, requestId) => Object.assign({id: requestId}, data), // attach requestId to message as `id` field
                    extractRequestId: data => data && data.id,                                  // read requestId from message `id` field
                });
                await this.agoraWs.open();
                console.info("Connected to local WS server for agora!");
                const result = await this.agoraWs.sendRequest({
                    action: 'join', token: response.token, channel_name: response.channel,
                    user_id: this.currentUser.user_id, speaker: this.isSpeaker,
                    app_id: response.agora_app_id
                }, {timeout: 8000});

                if (Number(result.ok) !== 0) {
                    throw new Error("Error while connecting to agora!");
                }

                this.agoraWs.onClose.addListener(event => {
                    console.log(`Connections closed: ${event.reason}`);
                    // this.leaveChannel(new Error(`Connections closed: ${event.reason}`));
                });

                this.agoraWs.onError.addListener(event => console.error(event));

                this.isMuting = true;
                const that = this;
                this.agoraWs.onMessage.addListener(data => {
                    data = JSON.parse(<string>data);
                    console.log(data);
                    const user = that.allUsers.find(value => Number(value.user_id) === Number(data.uid));
                    switch (data.action) {
                        case 'on_muted':
                            if (user) {
                                user.is_muted = true;
                            }
                            break;
                        case 'on_unmuted':
                            if (user) {
                                user.is_muted = false;
                            }
                            break;
                        case 'on_speaking':
                            if (user) {
                                user.is_speaking = Number(data.speaker) > 0;
                                if (user.is_speaking) {
                                    user.is_speaker = true;
                                    user.is_muted = false;
                                }
                            }
                            break;
                        case 'on_leave':
                            that.leaveChannel(new Error("Error happened on Agora side!"))
                            break;

                    }
                });
                console.log("agora init was successful");
                return response;
            } catch (e) {
                this.message = "Error while connecting to agora: " + e;
                console.error("Error while connecting to agora: " + e);
                throw e;
            }
        }


        protected extractMyInfoFromResponse(getChannelResponse: GetChannel) {
            for (let user of getChannelResponse.users) {
                if (Number(user.user_id) == Number(this.currentUser.user_id)) {
                    this.isSpeaker = user.is_speaker;
                    this.isModerator = user.is_moderator;
                }
            }
        }

        protected onInvitedAsSpeaker(channel: string, inviterId: string, inviterName: string) {
            this.$confirm('You were invited as speaker by ' + inviterName + ", continue?").then(res => {
                if (res) {
                    ChannelsService.acceptInvite(channel, Number(inviterId)).then(data => {
                        if (!data.success) {
                            console.debug(`Accept from ${inviterId} was not successful`);
                            return Promise.reject(`Accept from ${inviterId} was rejected`);
                        } else {
                            this.onBecomeSpeaker(true);
                            return data;
                        }
                    }).catch(reason => {
                        this.message = `Accept from ${inviterId} was not successful: ` + reason;
                        console.error(`Accept from ${inviterId} was not successful`, reason);
                    })
                } else {
                    ChannelsService.rejectInvite(channel, Number(inviterId));
                    this.$snackbar("You have rejected invite!", 'info');
                }
            })
        }

        protected onAddSpeaker(channel: string, userId: string, speaker: boolean) {
            if (String(userId) === String(this.currentUser.user_id)) {
                this.onBecomeSpeaker(speaker);
            } else {
                const idx = this.allUsers.findIndex(value => String(value.user_id) === String(userId));
                if (idx >= 0) {
                    const found = this.allUsers[idx];
                    found.raise_hands = false;
                    found.is_speaker = speaker;
                    if (!speaker) {
                        found.is_moderator = false;
                    }
                }
            }

        }

        protected onRemoveUser(channel: string, userId: string) {
            const idx = this.allUsers.findIndex(value => String(value.user_id) === String(userId));
            if (idx >= 0) {
                this.allUsers.splice(idx, 1);
            }


        }

        protected onMakeModerator(channel: string, userId: string, moderator: boolean) {
            if (String(userId) === String(this.currentUser.user_id)) {
                // update state
                this.rejoinChannel().then(value => {
                }).catch(reason => {
                    console.error("error on onMakeModerator " + reason);
                });
            } else {
                const idx = this.allUsers.findIndex(value => String(value.user_id) === String(userId));
                if (idx >= 0) {
                    const found = this.allUsers[idx];
                    found.is_moderator = moderator;
                    if (moderator) {
                        found.is_speaker = true;
                    }
                }
            }

        }

        protected onUserJoined(user: GetChannelUserProfile) {
            const idx = this.allUsers.findIndex(value => String(value.user_id) === String(user.user_id));
            if (idx < 0) {
                this.allUsers.push(user)
            } else {
                const found = this.allUsers[idx]
                if (found.is_speaker != user.is_speaker) {
                    this.allUsers.splice(idx, 1);
                    this.allUsers.push(user)
                } else {
                    found.is_speaker = user.is_speaker;
                    found.is_moderator = user.is_moderator;
                    found.is_invited_as_speaker = user.is_invited_as_speaker;
                }

            }
        }

        protected onUserLeft(user_id: number) {
            if (String(this.currentUser.user_id) === String(user_id)) {
                this.leaveChannel();
                return;
            }
            const idx = this.allUsers.findIndex(value => String(value.user_id) === String(user_id));
            if (idx >= 0) {
                this.allUsers.splice(idx, 1);
            }
        }

        protected onRaiseHands(user_id: number) {
            const idx = this.allUsers.findIndex(value => String(value.user_id) === String(user_id));
            if (idx >= 0) {
                const found = this.allUsers[idx];
                found.raise_hands = true;
            }
        }

        protected onUnRaiseHands(user_id: number) {
            const idx = this.allUsers.findIndex(value => String(value.user_id) === String(user_id));
            if (idx >= 0) {
                const found = this.allUsers[idx];
                found.raise_hands = false;
            }
        }

        protected leaveChannel(error?: Error) {
            console.log('leaving channel');
            if (this.isLeaving) {
                return;
            }
            this.isLeaving = true;
            try {
                if (this.pingTimeout) {
                    clearTimeout(this.pingTimeout);
                }
                if (this.reloadChannelInfoTimeout) {
                    clearTimeout(this.reloadChannelInfoTimeout);
                }
                if (error) {
                    this.$snackbar("You are leaving the channel", 'info');
                } else {
                    this.$snackbar("You are leaving the channel", 'info');
                }
                const router = this.$router;
                setTimeout(() => {
                    if (!this.routeChanged && router.currentRoute.name == 'JoinChannel') {
                        router.push({path: '/channels'}).catch(() => {
                        });
                    }
                }, 3000);
                this.leavePubNub();
                this.leaveAgora();
            } finally {
                this.isLeaving = false;
            }
        }

        @Watch('$route')
        onRouteChanged(newValue: Route) {
            this.routeChanged = true;
        }

        protected leaveAgora() {
            try {
                if (this.agoraWs) {
                    this.agoraWs?.sendRequest({action: 'leave', channel_name: this.getChannel?.channel});
                    const agoraWs = this.agoraWs;
                    this.agoraWs = null;
                    agoraWs.close();

                }
            } catch (e) {
                console.log(e)
            }
        }

        protected async leaveAgoraAsync() {
            try {
                if (this.agoraWs) {
                    await this.agoraWs?.sendRequest({action: 'leave', channel_name: this.getChannel?.channel});
                    const agoraWs = this.agoraWs;
                    this.agoraWs = null;
                    await agoraWs.close();
                }
            } catch (e) {
                console.log('on leave error', e)
            }
        }

        protected leavePubNub() {
            try {
                if (this.pubnub) {
                    this.pubnub.unsubscribeAll()
                    this.pubnub.stop()
                }
            } catch (e) {
                console.log(e)
            }
        }

        beforeDestroy() {
            console.log('leaving channel beforeDestroy');
            this.leaveChannel();
        }

        protected pingChannel() {
            ChannelsService.pingChannel(this.channelName).then(data => {
                console.log("made a ping");
                if (String(data.should_leave) == "true") {
                    console.log('leaving channel should_leave');
                    this.leaveChannel()
                }
            });
        }

        protected reloadChannelInfo() {
            this.loading = true
            ChannelsService.getChannel(this.channelName).then(data => {
                    this.getChannel = data;
                    this.allUsers = data.users;
                    this.extractMyInfoFromResponse(data);
                    console.log("refreshed channel data from the server");
                    this.loading = false;
                    return data;
                },
                (error) => {
                    // this.leaveChannel()
                    this.loading = false;
                    this.message = error;
                })
        }

        protected reloadChannelInfoRepeated() {
            this.reloadChannelInfoTimeout = setTimeout(() => {
                try {
                    this.reloadChannelInfo();
                } catch (e) {
                    console.error("error during reloadChannelInfo", e)
                }
                this.reloadChannelInfoRepeated();
            }, 120000);
            return this.reloadChannelInfoTimeout;
        }

        protected pingChannelRepeated() {
            this.pingTimeout = setTimeout(() => {
                try {
                    this.pingChannel();
                } catch (e) {
                    console.error("error during ping", e)
                }
                this.pingChannelRepeated();
            }, 30000);
            return this.pingTimeout;
        }

        protected onBecomeSpeaker(speaker: boolean) {
            this.rejoinChannel().then(value => {
                this.isMuting = true;
                if (speaker) {
                    this.$snackbar("You became a speaker!", 'info');
                } else {
                    this.$snackbar("You are not a speaker anymore!", 'info');
                }

                this.isSpeaker = speaker;
            }).catch(reason => {
                console.error("error on onBecomeSpeaker " + reason);
            });

        }

        protected flipMute() {
            if (!this.isSpeaker) {
                return;
            }
            if (this.isMuting) {
                this.onUnMute();
            } else {
                this.onMute();
            }
        }

        protected onMute() {
            if (this.agoraWs) {
                this.agoraWs.sendPacked({action: 'mute'});
            }
            this.$snackbar("You have muted!", 'info');
            this.isMuting = true;
        }

        protected onUnMute() {
            if (this.agoraWs) {
                this.agoraWs.sendPacked({action: 'unmute'});
            }
            this.$snackbar("You have unmuted!", 'info');
            this.isMuting = false;
        }

        protected flipRaiseHands() {
            if (this.isSpeaker) {
                return;
            }
            if (this.isHandsRaised) {
                this.unRaiseHand();
            } else {
                this.raiseHand();
            }
        }

        protected raiseHand() {
            if (this.isHandsRaised) {
                return;
            }
            this.loading = true
            ChannelsService.raiseHands(this.channelName).then(data => {
                    this.loading = false;
                    this.isHandsRaised = true;
                    this.$snackbar("You have raised your hands!", 'info');
                    return data;
                },
                (error) => {
                    this.loading = false;
                    this.message = error;
                })
        }

        protected unRaiseHand() {
            if (!this.isHandsRaised) {
                return;
            }
            this.loading = true
            ChannelsService.unRaiseHands(this.channelName).then(data => {
                    this.loading = false;
                    this.isHandsRaised = false;
                    this.$snackbar("You have unraised your hands!", 'info');
                    return data;
                },
                (error) => {
                    this.loading = false;
                    this.message = error;
                })
        }

        protected get speakers() {
            const res = this.allUsers.filter(value => value.is_speaker);
            console.info(res.length + " speakers");
            return res
        }

        protected get nonSpeakers() {
            const res = this.allUsers.filter(value => !value.is_speaker);
            console.info(res.length + " non speakers");
            return res.slice(0, 400);
        }

        protected get handsRaisedUsers() {
            const res = this.allUsers.filter(value => value.raise_hands);
            console.info(res.length + " raise_hands");
            return res
        }

        protected openProfile(id: string) {
            this.$router.push({name: 'Profile', params: {'id': "" + id}})
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

</style>