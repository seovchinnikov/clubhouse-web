import {PluginFunction} from 'vue'

export interface VueSnackbarQueueUseOptions extends VueSnackbarQueueObject {
    property?: string
}

declare const VueSnackbarQueuePlugin: VueSnackbarQueuePlugin
export default VueSnackbarQueuePlugin

export interface VueSnackbarQueuePlugin {
    install: PluginFunction<VueSnackbarQueueUseOptions>
}

export interface VueSnackbarQueueObject {

}

declare module 'vue/types/vue' {
    interface Vue {
        $snackbar(message: string, color: String)
    }
}
