import Vue from 'vue';
import App from './App.vue';
import router from './router';
import store from './store';
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';
import axios from 'axios';
import {extend} from "vee-validate";
import vuetify from './plugins/vuetify';
import {authHeader, refreshHeader, responseError} from "@/services/auth-header";
import {max, min, required} from "vee-validate/dist/rules";
import VuetifyConfirm from 'vuetify-confirm'
import VSnackbarQueue from '@tozd/vue-snackbar-queue';
axios.defaults.headers.post['Content-Type'] = 'application/json; charset=utf-8';
axios.defaults.headers.post['Accept'] = 'application/json';
axios.interceptors.request.use(authHeader);
axios.interceptors.response.use(refreshHeader, responseError);

Vue.config.productionTip = false;
Vue.config.devtools = true;

Vue.use(VuetifyConfirm, { vuetify })
Vue.use(VSnackbarQueue);

extend("min", min);
extend("max", max);
extend("required", required);

new Vue({
    router,
    store,
    vuetify,
    render: h => h(App)
}).$mount('#app');