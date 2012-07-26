
var uuid = "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g,function(a,b){return b=Math.random()*16,(a=="y"?b&3|8:b|0).toString(16)});

function AppViewModel() {

  var that = this;
  var eb = new vertx.EventBus(window.location.protocol + '//' + window.location.hostname + ':' + window.location.port + '/eventbus');
  
  that.chatMessage = ko.observable("");
  
  eb.onopen = function() {    
    eb.registerHandler('agora.out', function(body) {
      
      var chatOutputDiv = $('#chatOutput');

      var msg = '<p';

      if (body.self) { msg += ' class="me"'; }

      msg += '>'+body.nick;

      if (!body.self) { msg += ':'; }

      msg += ' '+body.text+'</p>';

      chatOutputDiv.append(msg);

      chatOutputDiv.scrollTop(chatOutputDiv[0].scrollHeight)
    });

    $.getJSON("/files", function(data) { 
      var filesDiv = $('#files');
      $.each(data.files, function(index, value) { 
        filesDiv.append('<p><a target="_blank" href="/files/'+value.name+'">'+value.name+'</a></p>');
      });
    });
  };


  eb.onclose = function() {
    eb = null;
  };

  that.sendMessage = function(data, event) {
    if (event.keyCode == 13) {
      var message = that.chatMessage();
      if (message.length > 0) {
        eb.send('agora', {uuid: uuid, text: message});
        that.chatMessage("");
        $('#chatInput').focus();
      }
    }
  };

};

ko.applyBindings(new AppViewModel());

$(function() {
  $('#chatInput').focus();
})