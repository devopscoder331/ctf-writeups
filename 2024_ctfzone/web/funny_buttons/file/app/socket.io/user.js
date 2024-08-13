const { db } = require('../db');

function getOnline(socket, data, callback){
    db.getOnlineUsers().then(res => {
        socket.emit('user.getOnline', {success: true, count: res});
    }).catch(err => callback(err));
}

function getInfo(socket, data, callback){
    socket.emit('user.getInfo', {
        success: true,
        info: {
            uname: socket.uname,
            uid: socket.uid
        }
    });
}

module.exports = {
    getOnline,
    getInfo
}