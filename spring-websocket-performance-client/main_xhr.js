var sjsc = require('sockjs-client');
var stomp = require('stomp');
var timers = require('timers');
var http = require('http');

var time = {}; 

if (process.argv.length != 3) {
	throw new Error("Expected Processor Identifier");  
} 

var username = process.argv[2];
// console.log("Running under username: ", username);  

// An object of options to indicate where to post to
var post_options = {
  host: 'localhost',
  port: '8080',
  path: '/spring-websocket-performance-endpoint/login',
  method: 'GET',
  headers: {
      'Content-Type': 'application/json',
      'Content-Length': 0,
      'username': username, 
      'password': username
  }
};
// Set up the request
var post_req = http.request(post_options, function(res) {
	res.setEncoding('utf8');
  	res.on('data', function (chunk) {
		var session = JSON.parse(chunk).session; 
    	var client = sjsc.create("http://localhost:8080/spring-websocket-performance-endpoint/comet", {
          debug: false,
          devel: false,
          protocols_whitelist: ['xhr-streaming']
        }); 
      	client.on('connection', function () { 
			var sclient = new stomp.Stomp({socket: client, log: function(){},}); 
			sclient.connect(); 
			sclient.on('connected', function() {
				client.set_cookies(session);

				timers.setInterval(function(){
					client.write('\n');
				}, 10000); 
				sclient.subscribe({
					'destination': '/topic/ping.1', 
					'id':'sub-0'
				}, function(msg){
					var current = new Date().getTime(); 
					if (time['sub-0'] !== undefined) old = time['sub-0']
					else old = current;
					console.log(username, "SUB-0", JSON.parse(msg).time, current - old);
					time['sub-0'] = current;  
				});
				sclient.subscribe({
					'destination': '/topic/ping.2', 
					'id':'sub-1'
				}, function(msg){
					var current = new Date().getTime(); 
					if (time['sub-1'] !== undefined) old = time['sub-1']
					else old = current;
					console.log(username, "SUB-1", JSON.parse(msg).time, current - old);
					time['sub-1'] = current;  
				}); 

				sclient.subscribe({
					'destination': '/topic/ping.3', 
					'id':'sub-2'
				}, function(msg){
					var current = new Date().getTime(); 
					if (time['sub-2'] !== undefined) old = time['sub-2']
					else old = current;
					console.log(username, "SUB-2", JSON.parse(msg).time, current - old);
					time['sub-2'] = current;  
				}); 
 
			}); 

		});

		client.on('error', function (e) { 
			console.log("ERROR",e); 
		});
  });
});

post_req.write('');
post_req.end();




