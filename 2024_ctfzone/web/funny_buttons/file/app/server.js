const express = require('express');
const session = require('express-session');
const http = require('http');
const { Server } = require("socket.io");

const exphbs  = require('express-handlebars');
const RedisStore = require("connect-redis").default
const { redisClient, db } = require('./db');
const { authorize, onConnection } = require('./socket.io')


const HOST = '0.0.0.0';
const PORT = process.env.PORT ?? '3000';
const SESSION_SECRET = process.env.SESSION_SECRET ?? 'secret';
const FLAG = process.env.FLAG ?? 'CTFZONE{redactedfunnyhere}';

const app = express();
const server = http.createServer(app);
const io = new Server(server);
const hbs = exphbs.create();

app.use(express.urlencoded({ extended: false }));
app.use(express.json());
app.use('/static/', express.static('./static'))

io.use(authorize);
io.on('connection', onConnection);

// views
app.engine('handlebars', hbs.engine);
app.set('view engine', 'handlebars');

// session
let sessionMiddleware = session({
    store: new RedisStore({ client: redisClient }),
    secret: SESSION_SECRET,
    resave: false,
    saveUninitialized: false,
    cookie: {
        secure: false,
        httpOnly: true,
    },
})
io.engine.use(sessionMiddleware);
app.use(sessionMiddleware);

// flash
app.use((req, res, next) => {
    const { render } = res;
    req.session.flash = req.session.flash ?? [];
    res.render = (template, options={}) => {
        render.call(res, template, {
            user: req.session?.user,
            flash: req.session.flash,
            ...options,
        });
        req.session.flash = [];
    };
    res.flash = (level, message) => {
        req.session.flash.push({ level, message });
    };
    next();
});

const ensureAuth = (req, res, next) => {
    if (!req.session?.user?.id) {
      res.flash('warning', 'Login required');
      return res.redirect('/login');
    }
    next();
};

app.get('/', ensureAuth, (req, res) => res.render('index', req.session.user));

app.get('/register', (req, res) => res.render('register'));
app.post('/register', async (req, res) => {
    try {
        await db.createUser(...Object.values(req.body));
        res.redirect('/login');
    } catch (error) {
        console.error('create user error:', error?.message)
        res.flash('danger', `Error: ${error?.message}`);
        res.render('register');
    }
});

app.get('/login', (req, res) => res.render('login'));
app.post('/login', async (req, res) => {
    const user = await db.getUserByNameAndPassword(...Object.values(req.body));
    if (!user) {
        res.flash('danger', 'invalid username or password');
        return res.status(401).render('login');
    }

    await db.addSessionToUser(user.id, req.sessionID);
    req.session.user = user;
    res.redirect('/');
});

app.post('/logout', ensureAuth, (req, res) => {
    const sid = req.sessionID;
    const uid = req.session.user.id;
    req.session.destroy(async (error) => {
        if (error) {
            console.error('logout error:', error?.message);
        }
        await db.removeSessionFromUser(uid, sid);
        res.clearCookie('connect.sid');
        res.redirect('/login');
    });
});

server.listen(PORT, HOST, () => console.log(`Listening on ${HOST}:${PORT}`));
