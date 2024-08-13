const async = require('async');
const cookieParser = require('cookie-parser')(process.env.SESSION_SECRET ?? 'secret');
const { redisClient, db } = require('../db');
const ratelimit = require('./ratelimit');

let Namespaces = {};
let onlineCount = 0;

requireModules();
db.flushOnlineUsers();
db.flushPressedButtons();

function onConnection(socket) {
	db.incrOnlineUsers();
	socket.ip = (socket.request.headers['x-forwarded-for'] || socket.request.connection.remoteAddress || '').split(',')[0];

	socket.onAny((eventName, params, callback) => {
		onMessage(socket, eventName, params, callback);
	});
    socket.on('disconnect', function() {
        onlineCount--;
		db.decrOnlineUsers();
    });
	socket.on("connect_error", (err) => {
		console.log(`connect_error due to ${err.message}`);
	});
}

function onMessage(socket, eventName, params, callback) {
	callback = typeof callback === 'function' ? callback : function () {};
	if (!eventName) {
		console.log('[socket.io] Empty method name');
		return callback({ message: '[socket.io] Empty method name' });
	}
	if (!params) {
		console.log('[socket.io] Empty payload');
		return callback({ message: '[socket.io] Empty payload' });
	}

	var parts = eventName.toString().split('.');
	var namespace = parts[0];
	var methodToCall = parts.reduce(function (prev, cur) {
		if (prev !== null && prev[cur]) {
			return prev[cur];
		}
		return null;
	}, Namespaces);

	if (!methodToCall) {
		if (process.env.NODE_ENV === 'development') {
			console.log('[socket.io] Unrecognized message: ' + eventName);
		}
		return callback({ message: '[[error:invalid-event]]' });
	}

	socket.previousEvents = socket.previousEvents || [];
	socket.previousEvents.push(eventName);
	if (socket.previousEvents.length > 20) {
		socket.previousEvents.shift();
	}

	if (ratelimit.isFlooding(socket)) {
		console.log('[socket.io] Too many emits! Disconnecting uid : ' + socket.uid + '. Events : ' + socket.previousEvents);
		return socket.disconnect();
	}

	async.waterfall([
		function (next) {
			if (Namespaces[namespace].before) {
				Namespaces[namespace].before(socket, eventName, params, next);
			} else {
				next();
			}
		},
		function (next) {
			methodToCall(socket, params, next);
		},
	], function (err, result) {
		console.log('result', err, result);
		callback(err ? { message: err.message } : null, result);
	});
}

function requireModules() {
	var modules = [
		'user',
		'button',
		'room'
	];

	modules.forEach(function (module) {
		Namespaces[module] = require('./' + module);
	});
}

function authorize(socket, callback) {
	let request = socket.request;
	if (!request) {
		return callback(new Error('[[error:not-authorized]]'));
	}

	async.waterfall([
		function (next) {
			cookieParser(request, {}, next);
		},
		function (next) {
			if (!request.session?.user?.id) {
				return next(new Error('[[error:not-authorized]]'));
			}
			socket.uid = parseInt(request.session.user.id, 10);
			socket.uname = request.session.user.name;
			next();
		},
	], callback);
}

module.exports = {
	authorize,
    onConnection
};
