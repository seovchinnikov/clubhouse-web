<template>
    <v-card>
        <v-card-title>
            Channels
            <v-spacer></v-spacer>
            <v-text-field
                    v-model="search"
                    append-icon="mdi-magnify"
                    label="Search"
                    single-line
                    hide-details
            ></v-text-field>
        </v-card-title>
        <v-data-table
                :headers="headers"
                :items="channels"
                :search="search" @click:row="openChannel"
        >
            <template v-slot:item.users="{ item }">
                <span v-for="user in item.users.slice(0, 8)" :key="user.user_id">
                    <v-tooltip bottom>
                        <template #activator="{ on }">
                            <v-avatar size="36" @click="openProfile(user.user_id)" v-on="on">
                                <v-img
                                        :src="user.photo_url"
                                        :alt="user.name"
                                ></v-img>
                             </v-avatar>
                        </template>
                        <span>{{user.name}}</span>
                    </v-tooltip>
                </span>
            </template>
        </v-data-table>
        <div class="form-group">
            <div v-if="message" class="alert alert-danger" role="alert">
                {{ message }}
            </div>
        </div>
    </v-card>
</template>

<script lang="ts">
    import {Component, Vue, Watch} from "vue-property-decorator";
    import ChannelsService from "@/services/ChannelsService";
    import {ChannelsResponseChannelResponse} from "@/model/ChannelsResponse";
    import {namespace} from "vuex-class";

    const Auth = namespace("Auth");
    @Component
    export default class ChannelsList extends Vue {
        @Auth.Getter
        private isLoggedIn!: boolean;

        @Auth.Getter
        private isActive!: boolean;

        private loading: boolean = false;
        private total: number = 0;
        private channels: ChannelsResponseChannelResponse[] = [];
        private message: string = "";
        private options: any;
        private headers = [
            {text: 'Channel', value: 'channel'},
            {text: 'Topic', value: 'topic'},
            {text: 'Users', value: 'num_all'},
            {text: 'Speakers', value: 'num_speakers'},
            {text: 'Users', value: 'users'},
            {text: 'Private', value: 'is_private'},
            // {text: 'Url', value: 'url'}
        ]

        mounted() {
            if (!this.isActive) {
                this.$router.push("/");
                return;
            }
            this.getDataFromApi()
        }

        @Watch('options')
        onPropertyChanged(value: string, oldValue: string) {
            this.getDataFromApi()
        }

        @Watch('loading')
        onLoadingChanged(value: string, oldValue: string) {
            this.$emit("loadingevent", value);
        }

        getDataFromApi() {
            this.loading = true
            ChannelsService.getChannels().then(data => {
                    this.channels = data.channels
                    this.total = data.channels.length
                    this.loading = false
                },
                (error) => {
                    this.loading = false;
                    this.message = error;
                });
        }

        openProfile(id: string) {
            this.$router.push({name: 'Profile', params: {'id': "" + id}})
        }

        protected openChannel(channel: any) {
            this.$confirm("Do you want to join this channel?").then(res => {
                if (res) {
                    this.$router.push({name: 'JoinChannel', params: {'channelName': "" + channel.channel}});
                }
            });

        }
    }
</script>

<style lang="css" scoped>
    .v-avatar {
        cursor: pointer !important;
    }

    a.v-btn:hover {
        text-decoration: none !important;
    }

    .my-12 {
        cursor: pointer !important;
    }


</style>
