<template>
    <div>
        <v-alert v-if="!this.isNoWait"
                 prominent
                 type="error"
        >
            <v-row align="center">
                <v-col class="grow">
                    You are in the wait list! Please wait until someone is inviting you and update this page!
                </v-col>
                <v-col class="shrink">
                    <v-btn @click.prevent="handleUpdate" :disabled="loading">Update</v-btn>
                    <span
                            v-show="loading"
                            class="spinner-border spinner-border-sm"
                    ></span>
                </v-col>
            </v-row>
        </v-alert>
        <v-alert v-if="message"
                 text
                 dense
                 color="teal"
                 icon="mdi-clock-fast"
                 border="left"
        >
            {{ message }}
        </v-alert>
        <v-alert v-if="errorMessage"
                 prominent
                 type="error"
        >
            {{ errorMessage }}
        </v-alert>
    </div>
</template>

<script lang="ts">
    import {Component, Vue, Watch} from "vue-property-decorator";
    import {namespace} from "vuex-class";

    const Auth = namespace("Auth");

    @Component
    export default class Login extends Vue {
        private loading: boolean = false;
        private message: string = "";
        private errorMessage: string = "";

        @Auth.Getter
        private isLoggedIn!: boolean;

        @Auth.Getter
        private isNoWait!: boolean;

        @Auth.Getter
        private isBoarded!: boolean;

        @Auth.Action
        private checkWaitList!: () => Promise<any>;

        created() {
            if (!this.isLoggedIn) {
                this.$router.push("/start_phone_number_auth");
            } else if (this.isNoWait) {
                if (!this.isBoarded) {
                    this.$router.push("/boarding");
                } else {
                    this.$router.push("/inner_profile");
                }
            }
        }

        mounted() {
            this.handleUpdate();
        }

        @Watch('loading')
        onLoadingChanged(value: string, oldValue: string) {
            this.$emit("loadingevent", value);
        }

        handleUpdate() {
            if (!this.isNoWait) {
                this.loading = true;
                this.checkWaitList().then(
                    (data) => {
                        this.loading = false;
                        this.errorMessage = "";
                        if (this.isNoWait) {
                            if (this.isBoarded) {
                                this.message = "You are now accepted to Club House, enjoy!";
                                setTimeout(() => this.$router.push({path: '/inner_profile'}).catch(), 4000);
                            } else {
                                this.message = "You have not finished your registration in the app!";
                                setTimeout(() => this.$router.push({path: '/boarding'}).catch(), 3000);
                            }
                        } else {
                            this.message = "You are still in the waitlist!";
                        }
                    },
                    (error) => {
                        this.message = "";
                        this.loading = false;
                        this.errorMessage = error;
                    }
                );
            } else {
                throw new Error("smth is wrong!");
            }
        }


    }
</script>


