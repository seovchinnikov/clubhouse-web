import Vue from 'vue';
import VueRouter, {RouteConfig} from "vue-router";
import PhoneAuthStart from "@/components/PhoneAuthStart.vue";
import UserProfile from "@/components/UserProfile.vue";
import Index from '@/store/index';
import Club from "@/components/Club.vue";
import Followers from "@/components/Followers.vue";
import Following from "@/components/Following.vue";


Vue.use(VueRouter);

const routes: Array<RouteConfig> = [
    {
        path: '/',
        component: PhoneAuthStart
    },
    {
        path: '/start_phone_number_auth',
        name: 'StartPhoneAuth',
        component: PhoneAuthStart
    },
    {
        path: '/inner_profile',
        name: 'inner profile',
        // lazy-loaded
        component: () => import('./components/InnerProfile.vue')
    },
    {
        path: '/waitlist',
        name: 'Waitlisted',
        // lazy-loaded
        component: () => import('./components/WaitList.vue')
    },
    {
        path: '/boarding',
        name: 'OnboardingWarning',
        // lazy-loaded
        component: () => import('./components/OnboardingWarning.vue')
    },
    {
        path: '/channels',
        name: 'Channels',
        // lazy-loaded
        component: () => import('./components/ChannelsList.vue')
    },
    {
        path: '/profile/:id',
        name: 'Profile',
        props: true,
        // lazy-loaded
        component: UserProfile
    },
    {
        path: '/suggested_follows',
        name: 'SuggestedFollows',
        // lazy-loaded
        component: () => import('./components/SuggestedFollows.vue')
    },
    {
        path: '/room/:channelName',
        name: 'JoinChannel',
        props: true,
        // lazy-loaded
        component: () => import('./components/JoinChannel.vue')
    }, {
        path: '/events',
        name: 'Events',
        // lazy-loaded
        component: () => import('./components/Events.vue')
    },
    {
        path: '/club/:id',
        name: 'Club',
        props: true,
        // lazy-loaded
        component: Club
    },
    {
        path: '/search',
        name: 'Search',
        // lazy-loaded
        component: () => import('./components/Search.vue')
    },
    {
        path: '/following/:id',
        name: 'Following',
        props: true,
        // lazy-loaded
        component: Following
    },
    {
        path: '/followers/:id',
        name: 'Followers',
        props: true,
        // lazy-loaded
        component: Followers
    },
    {
        path: '/notifications',
        name: 'Notifications',
        // lazy-loaded
        component: () => import('./components/Notifications.vue')
    },
    {
        path: '/my_profile',
        name: 'Profile',
        // lazy-loaded
        component: () => import('./components/MyProfile.vue')
    }
];

const router = new VueRouter({
    mode: "hash",
    base: process.env.BASE_URL,
    routes
});

// router.beforeEach((to, from, next) => {
//     console.log(Index)
//     console.log(Index.getters)
//     if (!Index.getters['Auth/isLoggedIn'] && to.name !== 'StartPhoneAuth') {
//         next({name: 'StartPhoneAuth'});
//     } else if (Index.getters['Auth/isLoggedIn'] && !Index.getters['Auth/isNoWait'] && to.name !== 'Waitlisted') {
//         next({name: 'Waitlisted'});
//     } else if (Index.getters['Auth/isLoggedIn'] && !Index.getters['Auth/isBoarded'] && to.name !== 'OnboardingWarning') {
//         next({name: 'OnboardingWarning'});
//     } else {
//         next();
//     }
// });
export default router;


