const redis = require('redis');
const argon2 = require('argon2');

const REDIS_URL = `redis://${process.env.REDIS_HOST}:${process.env.REDIS_PORT}` ?? 'redis://127.0.0.1:6379';

const redisClient = redis.createClient({
    url: REDIS_URL,
});
redisClient.on('connect', () => {
    console.log('Connected to redis');
});
redisClient.on('error', (error) => {
    console.log('redis error:' + error?.message);
});

redisClient.connect();

const rand = (max) => Math.floor(Math.random() * max);

const helpers = {
    async createUser(name, password) {
        const isAvailable = await redisClient.SETNX(`user:${name}`, 'PLACEHOLDER');
        if (!isAvailable) {
            throw new Error('user already exists!');
        }

        const uid = await redisClient.INCR('index:uid');
        await redisClient.SET(`user:${name}`, uid);

        const hash = await argon2.hash(password);
        await redisClient.HSET(`uid:${uid}`, { name, hash });
        return uid;
    },
    async getUser(uid) {
        const user = await redisClient.HGETALL(`uid:${uid}`);
        if (!user?.name) {
            return null;
        }
        user.id = uid;
        return user;
    },
    async getUserByNameAndPassword(name, password) {
        const uid = await redisClient.GET(`user:${name}`);
        if (!uid) {
            return null;
        }
        const user = await helpers.getUser(uid);
        if (!user) {
            return null;
        }
        try {
            if (await argon2.verify(user.hash, password)) {
                return user;
            } else {
                return null;
            }
        } catch (error) {
            console.log('argon error:', error?.message);
            return null;
        }
    },
    async getUserSessions(uid) {
        return redisClient.SMEMBERS(`uid:${uid}:sessions`);
    },
    async addSessionToUser(uid, sid) {
        return redisClient.SADD(`uid:${uid}:sessions`, sid);
    },
    async removeSessionFromUser(uid, sid) {
        return redisClient.SREM(`uid:${uid}:sessions`, sid);
    },
    async incrOnlineUsers() {
        return await redisClient.INCR('online');
    },
    async decrOnlineUsers() {
        return await redisClient.DECR('online');
    },
    async getOnlineUsers() {
        let online = await redisClient.GET('online');
        return Number(online);
    },
    async flushOnlineUsers() {
        return await redisClient.SET('online', 0);
    },

    async setButton(id, isPressed, uid) {
        isPressed = isPressed|0;
        return await redisClient.HSET(`button:${id}`, {isPressed, uid});
    },
    async getButton(id) {
        let data = await redisClient.HGETALL(`button:${id}`);
        return {
            isPressed: Number(data.isPressed),
            uid: Number(data.uid)
        }
    },
    async getAllButtons(){
        let result = [];
        for (let i = 0; i < 25; i++){
            let button = await redisClient.HGETALL(`button:${i+1}`);
            let pressed = !!+button.isPressed;
            let uname = pressed ? (await redisClient.HGET(`uid:${button.uid}`, 'name')) : NaN;
            let uid = pressed ? button.uid : NaN;
            result.push({
                id: i+1,
                pressed: pressed,
                uname: uname,
                uid: uid
            });
        }
        return result;
    },
    async flushPressedButtons() {
        for (let i = 1; i <= 25; i++){
            await redisClient.HSET(`button:${i}`, {isPressed: 0, uid: 0});
        }
    },

    async setFunny(id, uid, funny) {
        if ((await helpers.getFunny(id, uid)) === null)
            return await redisClient.SET(`button:${id}:${uid}:funny`, funny);
        return null;
    },
    async getFunny(id, uid) {
        return await redisClient.GET(`button:${id}:${uid}:funny`);
    },
};

module.exports = {
    redisClient,
    db: helpers,
    rand: rand
};
