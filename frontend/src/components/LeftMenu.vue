<template>
    <v-navigation-drawer app
                         v-model="drawer"
                         :mini-variant.sync="mini"
                         permanent
    >
        <v-list-item class="px-2">
            <v-list-item-avatar>
                <v-img :src="userVolatileInfo && userVolatileInfo.photo_url ? userVolatileInfo.photo_url : '/img/no-avatar.png'"></v-img>
            </v-list-item-avatar>

            <v-list-item-title>{{userVolatileInfo && userVolatileInfo.name ? userVolatileInfo.name :
                "Unauthenticated"}}
            </v-list-item-title>

            <v-btn
                    icon
                    @click.stop="mini = !mini"
            >
                <v-icon>mdi-chevron-left</v-icon>
            </v-btn>
        </v-list-item>

        <v-divider></v-divider>

        <v-list dense>
            <v-list-item v-if="currentUser && isActive"
                         link to="/events"
            >
                <v-list-item-icon>
                    <v-icon>event</v-icon>
                </v-list-item-icon>

                <v-list-item-content>
                    <v-list-item-title>Events</v-list-item-title>
                </v-list-item-content>
            </v-list-item>

            <v-list-item v-if="currentUser && isActive"
                         link to="/channels"
            >
                <v-list-item-icon>
                    <v-icon>list</v-icon>
                </v-list-item-icon>

                <v-list-item-content>
                    <v-list-item-title>Channels</v-list-item-title>
                </v-list-item-content>
            </v-list-item>
            <v-list-item v-if="currentUser && isActive"
                         link to="/search"
            >
                <v-list-item-icon>
                    <v-icon>search</v-icon>
                </v-list-item-icon>

                <v-list-item-content>
                    <v-list-item-title>Search</v-list-item-title>
                </v-list-item-content>
            </v-list-item>
            <v-list-item v-if="currentUser && isActive"
                         link to="/suggested_follows"
            >
                <v-list-item-icon>
                    <v-icon>list</v-icon>
                </v-list-item-icon>

                <v-list-item-content>
                    <v-list-item-title>Recommended follows</v-list-item-title>
                </v-list-item-content>
            </v-list-item>

            <v-list-item v-if="currentUser && !isNoWait"
                         link to="/waitlist"
            >
                <v-list-item-icon>
                    <v-icon>error</v-icon>
                </v-list-item-icon>

                <v-list-item-content>
                    <v-list-item-title>You are waitlisted!</v-list-item-title>
                </v-list-item-content>
            </v-list-item>
            <v-list-item v-if="currentUser && !isBoarded"
                         link to="/boarding"
            >
                <v-list-item-icon>
                    <v-icon>error</v-icon>
                </v-list-item-icon>

                <v-list-item-content>
                    <v-list-item-title>You have not registered!</v-list-item-title>
                </v-list-item-content>
            </v-list-item>
            <v-list-item v-if="currentUser && isActive"
                         link :to="{ name: 'Following', params: {id: String(currentUser.user_id) } }"
            >
                <v-list-item-icon>
                    <v-icon>person_add</v-icon>
                </v-list-item-icon>

                <v-list-item-content>
                    <v-list-item-title>Following</v-list-item-title>
                </v-list-item-content>
            </v-list-item>
            <v-list-item v-if="currentUser && isActive"
                         link :to="{ name: 'Followers', params: {id: String(currentUser.user_id) } }"
            >
                <v-list-item-icon>
                    <v-icon>people</v-icon>
                </v-list-item-icon>

                <v-list-item-content>
                    <v-list-item-title>Followers</v-list-item-title>
                </v-list-item-content>
            </v-list-item>
            <v-list-item v-if="currentUser && isActive"
                         link to="/notifications"
            >
                <v-list-item-icon>
                    <v-icon>alarm</v-icon>
                </v-list-item-icon>

                <v-list-item-content>
                    <v-list-item-title>Notifications</v-list-item-title>
                </v-list-item-content>
            </v-list-item>
            <v-list-item v-if="currentUser && isActive"
                         link to="/my_profile"
            >
                <v-list-item-icon>
                    <v-icon>face</v-icon>
                </v-list-item-icon>

                <v-list-item-content>
                    <v-list-item-title>My profile</v-list-item-title>
                </v-list-item-content>
            </v-list-item>
            <v-list-item v-if="currentUser"
                         link @click.prevent="logOut"
            >
                <v-list-item-icon>
                    <v-icon>lock_open</v-icon>
                </v-list-item-icon>

                <v-list-item-content>
                    <v-list-item-title>Log Out</v-list-item-title>
                </v-list-item-content>
            </v-list-item>
            <v-list-item v-if="currentUser && isActive"

            >
                <v-text-field @keyup.enter="enterClickedGoToChannel"
                              @click:append-outer="enterClickedGoToChannel"
                              v-model="channelName"
                              label="Join by channel id"
                              placeholder="Channel id"
                              append-outer-icon="mdi-send"
                              type="text"
                              filled
                              rounded
                              dense
                ></v-text-field>
            </v-list-item>

        </v-list>
    </v-navigation-drawer>
</template>

<script lang="ts">
    import {Component, Vue} from "vue-property-decorator";
    import {namespace} from "vuex-class";
    import {UserData, UserDataVolatileInfo} from "@/model/UserData";

    const Auth = namespace("Auth");

    @Component
    export default class App extends Vue {
        @Auth.State("user")
        private currentUser!: UserData;

        @Auth.State("userVolatileInfo")
        private userVolatileInfo!: UserDataVolatileInfo;

        @Auth.Action
        private signOut!: () => void;

        @Auth.Getter
        private isLoggedIn!: boolean;

        @Auth.Getter
        private isActive!: boolean;

        @Auth.Getter
        private isNoWait!: boolean;

        @Auth.Getter
        private isBoarded!: boolean;

        private drawer: boolean = true;
        private mini: boolean = false;
        private channelName: string = "";

        logOut() {
            this.signOut();
            this.$router.push("/start_phone_number_auth");
        }

        enterClickedGoToChannel() {
            if (this.channelName.length > 5) {
                this.$router.push({name: 'JoinChannel', params: {'channelName': "" + this.channelName.trim()}});
            }
        }
    }
</script>

<style scoped>
    .v-list-item:hover {
        text-decoration: none !important;
    }

</style>