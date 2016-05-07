/* Hello, World! program in node.js */
var http = require("http");
var Firebase = require("firebase");
var fb = new Firebase("https://stazo-project-18.firebaseio.com/");
http.createServer(function (request, response) {
    // Send the HTTP header
    // HTTP Status: 200: OK
    // Content Type: text/plain
    response.writeHead(200, {'Content-Type': 'text/plain'});

    // Send the response body as "Hello World"
    response.end('Hello World\n');
}).listen(8081);

console.log("Hello, World!")
deleteEvents();
function callEveryHour() {
     setInterval(deleteEvents, 1000 * 60 * 60);
}
 
function deleteEvents() {
	fb.child("Events").once("value", function(snapshot) {
		snapshot.forEach(function(childSnapshot) {
        	if (childSnapshot.child("startTime").val() == 1111) {
            	childSnapshot.ref().remove();
        	}
    	});
	});
}
