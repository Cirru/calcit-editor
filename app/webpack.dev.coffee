path = require 'path'
resolve = require('path').resolve
webpack = require 'webpack'

module.exports =
  entry:
    main: './entry/dev'
  devServer:
    clientLogLevel: 'info'
    stats: 'errors-only'
    contentBase: resolve(__dirname, 'target')
    publicPath: '/'
    host: '0.0.0.0'
  output:
    filename: '[name].js'
  module:
    rules: [
      test: /\.css$/
      loaders: ['style-loader', 'css-loader']
    ,
      test: /\.(eot|svg|ttf|woff2?)(\?.+)?$/
      loader: 'url-loader'
      query:
        limit: 100
        name: 'fonts/[name].[ext]'
    ]
  plugins: [
    new webpack.HotModuleReplacementPlugin(),
    new webpack.NamedModulesPlugin()
  ]
