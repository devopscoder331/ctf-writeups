function join(socket, data, callback){
    if (!data?.room)
        return callback(new Error('invalid room'))
    socket.join(data.room);
}

module.exports = {
	join
};
