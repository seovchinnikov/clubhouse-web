module.exports = {
    devServer: {
        port: 8081
    },

    configureWebpack: {
        devtool: 'source-map'
    },

    chainWebpack: config => config.optimization.minimize(false),

    transpileDependencies: [
        'vuetify'
    ]
}
