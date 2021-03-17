<template>
    <v-container>
        <v-row>
            <v-col cols="1" v-for="user in this.users">
                <user-avatar :user="user" :moderator="moderator" :channel="channel">

                </user-avatar>
            </v-col>
        </v-row>
    </v-container>
</template>

<script lang="ts">
    import {Component, Prop, Vue} from "vue-property-decorator";
    import {namespace} from "vuex-class";
    import {GetChannelUserProfile} from "@/model/GetChannel";
    import UserAvatar from "@/components/UserAvatar.vue";

    const Auth = namespace("Auth");
    @Component({
        components: {
            UserAvatar
        },
    })
    export default class UserAvatarList extends Vue {
        @Prop({type: Array, required: true})
        users: GetChannelUserProfile[] = [];

        @Prop({type: Boolean, required: true})
        moderator: boolean = false;

        @Prop({type: String, required: true})
        channel: string = "";
    }
</script>