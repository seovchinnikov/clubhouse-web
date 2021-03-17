<template>
    <div class="col-md-12">
        <div class="card card-container">
            <img
                    id="profile-img"
                    src="//ssl.gstatic.com/accounts/ui/avatar_2x.png"
                    class="profile-img-card"
            />
            <ValidationObserver ref="observer" v-slot="{ invalid }">
                <form name="form" @submit.prevent="handleSubmit">
                    <div v-if="step === 'START'" class="form-group">
                        <ValidationProvider name="phone" rules="required|max:12|min:5" v-slot="{ errors }">
                            <label for="phone">Phone</label>
                            <input
                                    v-model="user.phone"
                                    type="text"
                                    class="form-control"
                                    name="username"
                            />
                            <div
                                    v-if="errors.length"
                                    class="alert alert-danger"
                                    role="alert"
                            >
                                Phone is required!
                            </div>
                        </ValidationProvider>
                    </div>
                    <div v-if="step === 'VERIFY'" class="form-group">
                        <ValidationProvider name="code" rules="required|min:3" v-slot="{ errors }">
                            <label for="code">Verify code</label>
                            <input
                                    v-model="user.code"
                                    type="text"
                                    class="form-control"
                                    name="code"
                            />
                            <div
                                    v-if="errors.length"
                                    class="alert alert-danger"
                                    role="alert"
                            >
                                Code is required!
                            </div>
                        </ValidationProvider>
                    </div>
                    <div class="form-group">
                        <button class="btn btn-primary btn-block" :disabled="loading">
            <span
                    v-show="loading"
                    class="spinner-border spinner-border-sm"
            ></span>
                            <span>Confirm</span>
                        </button>
                    </div>
                    <div class="form-group">
                        <div v-if="message" class="alert alert-danger" role="alert">
                            {{ message }}
                        </div>
                    </div>
                </form>
            </ValidationObserver>
            <div>
                <v-btn color="primary" :disabled="this.step!=='VERIFY' || this.loading"
                       @click="back()"
                       class="ma-2 white--text linked_element">
                    <v-icon large left
                            color="blue lighten-1">arrow_back
                    </v-icon>
                    <span>Back</span>
                </v-btn>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import {Component, Vue, Watch} from "vue-property-decorator";
    import {namespace} from "vuex-class";
    import {ValidationObserver, ValidationProvider} from 'vee-validate';

    const Auth = namespace("Auth");

    @Component({
        components: {
            ValidationProvider,
            ValidationObserver
        },
    })
    export default class Login extends Vue {
        $refs!: {
            observer: InstanceType<typeof ValidationObserver>;
        };
        private user: any = {phone: "", code: ""};
        private loading: boolean = false;
        private message: string = "";

        private step: string = "START";
        private cookie?: string;

        @Auth.Getter
        private isLoggedIn!: boolean;

        @Auth.Getter
        private isNoWait!: boolean;

        @Auth.Getter
        private isBoarded!: boolean;

        @Auth.Action
        private startPhoneAuth!: (data: any) => Promise<any>;

        @Auth.Action
        private finishPhoneAuth!: (data: any) => Promise<any>;

        @Watch('loading')
        onLoadingChanged(value: string, oldValue: string) {
            this.$emit("loadingevent", value);
        }

        created() {
            if (this.isLoggedIn) {
                if (!this.isNoWait) {
                    this.$router.push("/waitlist");
                } else if (!this.isBoarded) {
                    this.$router.push("/boarding");
                } else {
                    this.$router.push("/inner_profile");
                }
            }
        }

        handleSubmit() {
            if (this.step === "START") {
                this.handleStart()
            } else if (this.step === "VERIFY") {
                this.handleVerify()
            } else {
                throw new Error("smth is wrong!");
            }
        }

        handleStart() {
            this.$refs.observer.validate().then((isValid) => {
                if (!isValid) {
                    this.loading = false;
                    return;
                }

                if (this.user.phone) {
                    this.loading = true;
                    this.startPhoneAuth(this.user.phone.trim()).then(
                        (data) => {
                            this.step = "VERIFY";
                            this.cookie = data.cookie;
                            this.loading = false;
                            this.message = "";
                        },
                        (error) => {
                            this.loading = false;
                            this.message = error;
                        }
                    );
                } else {
                    throw new Error("smth is wrong!");
                }
            });
        }

        handleVerify() {
            this.$refs.observer.validate().then((isValid) => {
                if (!isValid) {
                    this.loading = false;
                    return;
                }
                if (this.user.phone && this.user.code && this.cookie) {
                    this.loading = true;
                    this.finishPhoneAuth({
                        'phone': this.user.phone.trim(),
                        'cookie': this.cookie!!,
                        'verification_code': this.user.code.trim()
                    }).then(
                        (data) => {
                            this.loading = false;
                            this.message = "";
                            if (!this.isNoWait) {
                                this.$router.push("/waitlist");
                            } else if (!this.isBoarded) {
                                this.$router.push("/OnboardingWarning");
                            } else {
                                this.$router.push("/inner_profile");
                            }
                        },
                        (error) => {
                            this.loading = false;
                            this.message = error;
                        }
                    );
                } else {
                    throw new Error("smth is wrong!");
                }
            });
        }

        protected back() {
            if (this.step !== 'VERIFY') {
                return;
            }
            this.step = 'START';
            this.message = "";
            this.user['code'] = "";
        }
    }
</script>

<style scoped>
    label {
        display: block;
        margin-top: 10px;
    }

    .card-container.card {
        max-width: 350px !important;
        padding: 40px 40px;
    }

    .card {
        background-color: #f7f7f7;
        padding: 20px 25px 30px;
        margin: 0 auto 25px;
        margin-top: 50px;
        -moz-border-radius: 2px;
        -webkit-border-radius: 2px;
        border-radius: 2px;
        -moz-box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.3);
        -webkit-box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.3);
        box-shadow: 0px 2px 2px rgba(0, 0, 0, 0.3);
    }

    .profile-img-card {
        width: 96px;
        height: 96px;
        margin: 0 auto 10px;
        display: block;
        -moz-border-radius: 50%;
        -webkit-border-radius: 50%;
        border-radius: 50%;
    }
</style>
