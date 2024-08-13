let ratelimit = module.exports;

let allowedCalls = 200;
let timeframe = 10000;

ratelimit.isFlooding = function (socket) {
	socket.callsPerSecond = socket.callsPerSecond || 0;
	socket.elapsedTime = socket.elapsedTime || 0;
	socket.lastCallTime = socket.lastCallTime || Date.now();

	socket.callsPerSecond += 1;

	var now = Date.now();
	socket.elapsedTime += now - socket.lastCallTime;

	if (socket.callsPerSecond > allowedCalls && socket.elapsedTime < timeframe) {
		console.log('Flooding detected! Calls : ' + socket.callsPerSecond + ', Duration : ' + socket.elapsedTime);
		return true;
	}

	if (socket.elapsedTime >= timeframe) {
		socket.elapsedTime = 0;
		socket.callsPerSecond = 0;
	}

	socket.lastCallTime = now;
	return false;
};
