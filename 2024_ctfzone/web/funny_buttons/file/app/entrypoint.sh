#!/bin/bash

while ! nc -z $REDIS_HOST $REDIS_PORT; do
    sleep 0.1
done

# admin sending funny or smth
node <<-EOF
const { redisClient, db } = require('./db');
const crypto = require("crypto");
const funny = process.env.FLAG ?? 'CTFZONE{redactedfunnyhere}';

(async () => {
    let user = await db.getUser(1);
    if (!user?.name){
        let username = 'admin';
        let password = crypto.randomBytes(20).toString('hex');
        await db.createUser(username, password);

        let buttonId = Math.floor(Math.random() * 24)+1
        await db.setButton(buttonId, true, 1);
        await db.setFunny(buttonId, 1, funny);
    }
    process.exit();
})();
EOF

node server.js