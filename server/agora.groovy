def nicks = [:]

def adjectives = [ 'adorable', 'beautiful', 'strong', 'elegant', 'fancy', 'awesome', 'omnipotent', 'ugly', 'big', 'black', 'frigid', 'putrid' ]

def nouns = ['vampire','troll','dwarf','samurai','ninja','goblin','ice','blob']

def getNick = { uuid ->

	if (!nicks[uuid]) {

		def newNick = ''

		while (!newNick || nicks?.values?.contains(newNick)) {
			newNick = adjectives[new Random().nextInt(adjectives.size())]
			newNick += '_'+nouns[new Random().nextInt(nouns.size())]
			newNick += '_'+new Random().nextInt(10000)
		}

		nicks[uuid] = newNick
	}

	nicks[uuid]
}

vertx.eventBus.registerHandler('agora', { message ->

  String msg = message.body.text
  boolean self = msg.startsWith('/me ')

  if (self) { msg -= '/me ' }
  
  vertx.eventBus.publish('agora.out', [nick: getNick(message.body.uuid), text: msg, self: self])
})