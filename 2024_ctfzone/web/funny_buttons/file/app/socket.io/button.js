const { db } = require('../db');

function getAll(socket, data, callback){
    db.getAllButtons().then(res => {
        socket.emit('button.getAll', {success: true, buttons: res});
    });
}

function get(socket, data, callback){
    if (!data?.id || data?.id < 1 || data?.id > 25){
        return callback(new Error('invalid button id'));
    }

    db.getButton(data.id).then(button => {
        if (!button?.isPressed || button?.uid !== socket.uid){
            return callback(new Error('you should press button first!'));
        }

        db.getFunny(data.id, socket.uid).then(res => {
            socket.emit('button.get', {success: true, id: data.id, funny: res});
        });
    }).catch(err => callback(err));
}

function set(socket, data, callback){
    if (!data?.id || data?.id < 1 || data?.id > 25)
        return callback(new Error('invalid button id'));

    if (!data?.funny)
        return callback(new Error('no funny?'));

    db.getButton(data.id).then(button => {
        if (!button?.isPressed || button?.uid !== socket.uid){
            return callback(new Error('you should press button first!'));
        }
        
        db.setFunny(data.id, socket.uid, data.funny).then(res => {
            socket.emit('button.set', {success: true});
        });
    }).catch(err => callback(err));
}

function press(socket, data, callback){
    if (!data?.id || data?.id < 1 || data?.id > 25)
        return callback(new Error('invalid button id'));

    db.getButton(data.id).then(async button => {
        db.setButton(data.id, !(button?.isPressed|0), socket.uid).then(res => {
            let pressed = !(button?.isPressed|0);
            let uname = pressed ? socket.uname.substring(0,8) : NaN;
            let uid = pressed ? socket.uid : NaN;
            let result = {
                success: true,
                id: data.id,
                pressed: pressed,
                uname: uname,
                uid: uid
            };
            socket.emit('button.press', result);
            socket.in('online_users').emit('button.press', result);
        });
    }).catch(err => callback(err));
}

module.exports = {
    getAll,
    get,
    set,
    press
}