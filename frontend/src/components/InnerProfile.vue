<template>
    <v-card>
        <header class="jumbotron">
            <h3>
                <strong>{{ currentUser ? currentUser.username : ""}}</strong> Profile
                <span
                        v-show="loading"
                        class="spinner-border spinner-border-sm"
                ></span>
            </h3>
        </header>
        <p>
            <v-alert v-if="errorMessage"
                     prominent
                     type="error"
            >
                {{ errorMessage }}
            </v-alert>
        </p>
        <p>
            <strong>Token:</strong>
            {{ currentUser ? currentUser.token.substring(0, 6) : ""}} ...
            {{ currentUser ? currentUser.token.substr(currentUser.token.length - 6) : "" }}
        </p>
        <p>
            <strong>CH Token:</strong>
            {{ currentUser ? currentUser.user_token : "" }}
        </p>
        <p>
            <strong>Id:</strong>
            {{ currentUser ? currentUser.user_id : "" }}
        </p>
        <p>
            <strong>Name:</strong>
            {{ currentUser ? currentUser.name : "" }}
        </p>
        <p>
            <strong>Username:</strong>
            {{ currentUser ? currentUser.username : "" }}
        </p>
        <strong>Authorities:</strong>
        <ul>
            <li v-for="(role, index) in currentUser ? currentUser.roles : []" :key="index">
                {{ role }}
            </li>
        </ul>
    </v-card>
</template>

<script lang="ts">
    import {Component, Vue, Watch} from "vue-property-decorator";
    import {namespace} from "vuex-class";

    const Auth = namespace("Auth");

    @Component
    export default class InnerProfile extends Vue {
        private loading: boolean = false;
        private errorMessage: string = "";
        @Auth.State("user")
        private currentUser!: any;
        @Auth.Getter
        private isLoggedIn!: boolean;

        @Auth.Action
        private refreshTokenInfo!: (data: any) => Promise<any>;

        @Auth.Getter
        private isActive!: boolean;

        created() {
            if (!this.isLoggedIn) {
                this.$router.push("/start_phone_number_auth");
            } else if (this.isActive) {
                this.$router.push("/my_profile");
            }
        }

        mounted() {
            this.reload()
        }

        @Watch('loading')
        onLoadingChanged(value: string, oldValue: string) {
            this.$emit("loadingevent", value);
        }

        protected reload() {
            if (!this.currentUser) {
                return;
            }
            this.loading = true;
            this.refreshTokenInfo(this.currentUser.token).then(
                (data) => {
                    this.errorMessage = "";
                    this.loading = false;
                },
                (error) => {
                    this.errorMessage = error;
                    this.loading = false;
                }
            );
        }
    }
</script>
