
// Configuration for the web server
def webServerConf = [
  host: '0.0.0.0',
  port: 8080
]

container.with {
  deployVerticle('server/server.groovy', webServerConf)
  deployVerticle('server/agora.groovy')
}