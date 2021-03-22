<template>
  <v-card>
    <v-card-title>
      Update your bio
    </v-card-title>

    <v-container>
      <v-textarea
          rows="1"
          auto-grow
          v-model="bio"
      >
      </v-textarea>

      <div class="text-center">
        <v-btn
            rounded
            color="primary"
            dark
            @click="updateBio()"
        >
          Done
        </v-btn>
      </div>
    </v-container>

    <div class="form-group">
      <div v-if="message" class="alert alert-danger" role="alert">
        {{ message }}
      </div>
    </div>
  </v-card>
</template>

<script lang="ts">
import {Component, Vue, Watch} from "vue-property-decorator";
import {namespace} from "vuex-class";
import UsersService from "@/services/UsersService";

const Auth = namespace("Auth");
@Component
export default class UpdateBio extends Vue {
  @Auth.Getter
  private isLoggedIn!: boolean;
  @Auth.Getter
  private isActive!: boolean;
  @Auth.State("user")
  private currentUser!: any;

  private bio: string = "";
  private loading: boolean = false;
  private message: string = "";

  mounted() {
    if (!this.isActive) {
      this.$router.push("/");
      return;
    }
    this.getDataFromApi();
  }

  @Watch('loading')
  onLoadingChanged(value: string, oldValue: string) {
    this.$emit("loadingevent", value);
  }

  getDataFromApi() {
    this.loading = true;
    UsersService.getUser(this.currentUser.user_id).then(data => {
          this.bio = data.user_profile ? data.user_profile.bio : ""
          this.loading = false;
        },
        (error) => {
          this.loading = false;
          this.message = error;
        });
  }

  updateBio() {
    this.loading = true;
    UsersService.updateBio(this.bio).then(() => {
          const waitUntilDataUpdatedMs = 2000;
          setTimeout(() => {
            this.loading = false;
            this.$router.push({path: "/my_profile"}).catch()
          }, waitUntilDataUpdatedMs);
        },
        (error) => {
          this.loading = false;
          this.message = error;
        });
  }
};
</script>

<style scoped>

</style>