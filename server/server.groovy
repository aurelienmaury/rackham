import static org.vertx.groovy.core.streams.Pump.createPump

import org.vertx.groovy.core.http.RouteMatcher

int port = container.config['port'] ?: 80
String host = container.config['host'] ?: '0.0.0.0'
createServer().listen(port, host)

println "Server started on ${host}:${port}"

/**
 * Creates Http Main Server
 *
 */
def createServer() {

    def httpServer = vertx.createHttpServer()

    addRoutes(httpServer)

    initSockJS(httpServer)

    httpServer
}

void addRoutes(httpServer) {
    def routeMatcher = new RouteMatcher()

    String repo = new File('repo').absolutePath + File.separator
    String webRootPrefix = 'web/'
    String indexPage = webRootPrefix + 'index.html'

    routeMatcher.get('/') { req -> req.response.sendFile(indexPage) }

    routeMatcher.getWithRegEx('/static/(.*)$') { req ->
        if (!req.path.contains("..")) {
            req.response.sendFile(webRootPrefix + req.params['param0'])
        } else {
            req.response.with {
                statusCode = 403
                statusMessage = 'forbidden'
                end()
            }
        }
    }

    routeMatcher.get('/files') { req ->
        if (!req.path.contains("..")) {

            req.response.chunked = true
            req.response.putHeader('Content-Type', 'application/json')

            vertx.fileSystem.readDir(repo, '.*\\.txt') { ar ->
                req.response << '{"files":['

                if (ar.succeeded() && ar.result) {
                    req.response << ar.result.collect { fileName ->
                        '{"name":"' + (fileName - repo) + '"}'
                    }.join(',')
                }
                req.response << ']}'
                req.response.end()
            }
        } else {
            req.response.with {
                statusCode = 403
                statusMessage = 'forbidden'
                end()
            }
        }
    }

    routeMatcher.getWithRegEx('/files/(.*)$') { req ->
        if (!req.path.contains("..")) {
            vertx.fileSystem.exists(repo + req.params['param0']) { ar ->
                if (ar.succeeded() && ar.result) {
                    req.response.sendFile(repo + req.params['param0'])
                } else {
                    req.response.with {
                        statusCode = 404
                        statusMessage = 'these aren\'t the droids you\'re looking for'
                        end()
                    }
                }
            }
        } else {
            req.response.with {
                statusCode = 403
                statusMessage = 'forbidden'
                end()
            }
        }
    }

    routeMatcher.post('/upload') { req ->
        req.pause()
        def filename = repo + "${UUID.randomUUID()}.uploaded"
        vertx.fileSystem.open(filename) { ares ->
            def file = ares.result

            println "req => " + req.toStore

            def pump = createPump(req.params['toStore'], file.writeStream)

            req.endHandler {
                file.close {
                    println "Uploaded ${pump.bytesPumped} bytes to $filename"
                    req.response.end()
                }
            }

            pump.start()
            req.resume()
        }
    }

    routeMatcher.noMatch { req ->
        req.response.with {
            statusCode = 404
            statusMessage = 'not found'
            end()
        }
    }

    httpServer.requestHandler(routeMatcher.asClosure())
}

/**
 *
 * @param httpServer
 * @return
 */
void initSockJS(httpServer) {
    def sockJSServer = vertx.createSockJSServer(httpServer)

    def inboundPermitted = []
    inboundPermitted << [address: 'agora']

    def outboundPermitted = []
    outboundPermitted << [address: 'agora.out']

    sockJSServer.bridge([prefix: "/eventbus"], inboundPermitted, outboundPermitted)

    httpServer
}


