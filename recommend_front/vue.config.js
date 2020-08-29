// vue.config.js
module.exports = {
    // 基本路径
    publicPath: '.',
    devServer: {
        host: '0.0.0.0',
        port: 8080,
        proxy: {
            '/business': {
                target: 'http://127.0.0.1:8090',
                changeOrigin: true,
                ws: true,
                pathRewrite: {
                    '^/business': '/'
                }
            }
        },
        disableHostCheck: true,
    }
}