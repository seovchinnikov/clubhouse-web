<template>
    <v-app>
        <left-menu></left-menu>
        <top-menu :loading="loading"></top-menu>

        <v-main>
            <v-container fluid>
                <router-view @loadingevent="updateLoading"/>
                <v-snackbar-queue></v-snackbar-queue>
            </v-container>
        </v-main>

        <v-footer app>
            <!-- -->
        </v-footer>
    </v-app>
</template>

<script lang="ts">
    import {Component, Vue, Watch} from "vue-property-decorator";
    import {namespace} from "vuex-class";
    import TopMenu from "@/components/TopMenu.vue";
    import LeftMenu from "@/components/LeftMenu.vue";
    import {UserDataVolatileInfo} from "@/model/UserData";

    const Auth = namespace("Auth");
    @Component({
        components: {LeftMenu, TopMenu}
    })
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

        @Auth.Action
        private updateVolatileUserInfo!: () => Promise<UserDataVolatileInfo>;

        private loading: boolean = false;
        private userInfoUpdateTimeout?: number;

        @Watch('$route')
        onPropertyChanged(value: string, oldValue: string) {
            this.loading = false;
        }

        protected updateLoading(loading: boolean) {
            this.loading = loading;
        }

        mounted() {
            this.userInfoUpdateTimeout = this.updateMeInfo();
        }

        beforeDestroy() {
            clearTimeout(this.userInfoUpdateTimeout);
        }

        protected updateMeInfo() {
            if (!this.isLoggedIn || !this.isActive) {
                return;
            }
            this.updateVolatileUserInfo().catch(e => console.log(e));
            return setTimeout(() => {
                this.userInfoUpdateTimeout = this.updateMeInfo();
            }, 60000 + Math.floor((Math.random() * 60000)));
        }
    }
</script>
