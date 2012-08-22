
// Configuration for the web server
def webServerConf = [
  host: container.config['host'] ?: '0.0.0.0',
  port: container.config['port'] ?: 8080,
  repository: container.config['repository'] ?: '.'
]

container.with {
  deployVerticle('server/server.groovy', webServerConf)
  deployVerticle('server/agora.groovy')
}
