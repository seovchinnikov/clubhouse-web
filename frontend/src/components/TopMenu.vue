<template>
    <v-app-bar app>
        <v-toolbar-title>
            <router-link to="/" tag="span" style="cursor: pointer">
                <v-avatar size="64">
                    <v-img
                            src="/img/ch_logo.png"
                    ></v-img>
                </v-avatar>
                ClubHouse webapp
            </router-link>
        </v-toolbar-title>
        <v-progress-linear
                :active="loading"
                color="blue darken-2 accent-4"
                :indeterminate="true"
                absolute
                bottom
                height="4"></v-progress-linear>
        <v-spacer></v-spacer>
        <v-toolbar-items class="hidden-xs-only">
            <v-btn
                    text to="/">
                <v-icon left dark>home</v-icon>
                Home
            </v-btn>
            <v-btn v-if="currentUser && isActive"
                   text to="/events">
                <v-icon left dark>event</v-icon>
                Events
            </v-btn>
            <v-btn v-if="currentUser && isActive"
                   text to="/channels">
                <v-icon left dark>list</v-icon>
                Channels
            </v-btn>
            <v-btn v-if="currentUser && isActive"
                   text to="/search">
                <v-icon left dark>search</v-icon>
                Search
            </v-btn>
            <v-btn v-if="currentUser && isActive"
                   text to="/suggested_follows">
                <v-icon left dark>list</v-icon>
                Recommended follows
            </v-btn>
            <v-btn v-if="currentUser && isActive"
                   text to="/notifications">
                <v-icon left dark>alarm</v-icon>
                Notifications
            </v-btn>
            <v-btn v-if="currentUser && !isNoWait"
                   text to="/waitlist">
                <v-icon left dark>error</v-icon>
                You are waitlisted!
            </v-btn>
            <v-btn v-if="currentUser && !isBoarded"
                   text to="/boarding">
                <v-icon left dark>error</v-icon>
                You have not registered!
            </v-btn>
            <v-btn v-if="!currentUser"
                   text to="/start_phone_number_auth">
                <v-icon left dark>lock_open</v-icon>
                Sign In
            </v-btn>
            <v-btn v-if="currentUser && !isActive"
                   text to="/inner_profile">
                <v-icon left dark>face</v-icon>
                Profile
            </v-btn>
            <v-btn v-if="currentUser && isActive"
                   text to="/my_profile">
                <v-icon left dark>face</v-icon>
                Profile
            </v-btn>
            <v-btn v-if="currentUser"
                   text @click.prevent="logOut">
                <v-icon left dark>lock_open</v-icon>
                LogOut
            </v-btn>
        </v-toolbar-items>
    </v-app-bar>
</template>

<script lang="ts">
    import {Component, Prop, Vue} from "vue-property-decorator";
    import {namespace} from "vuex-class";

    const Auth = namespace("Auth");

    @Component
    export default class App extends Vue {
        @Auth.State("user")
        private currentUser!: any;

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

        @Prop({type: Boolean, required: true})
        private loading: Boolean = false;

        logOut() {
            this.signOut();
            this.$router.push("/start_phone_number_auth");
        }
    }
</script>

<style scoped>
    a.v-btn:hover {
        text-decoration: none !important;
    }

</style>