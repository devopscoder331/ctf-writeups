const express = require('express');
const path = require('path');
const session = require('express-session');
const MongoStore = require('connect-mongo');
const connectDB = require('./config/db');
const track = require('./middleware/track');
const authRoutes = require('./routes/authRoutes');
const fileRoutes = require('./routes/fileRoutes');
const previewRoutes = require('./routes/previewRoutes');

connectDB();

const app = express();

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use(session({
  secret: process.env.SESSION_SECRET,
  resave: false,
  saveUninitialized: false,
  cookie: {
    maxAge: 1000 * 60 * 60 * 24,
    secure: process.env.NODE_ENV === 'production',
    httpOnly: true
  }
}));

app.use(track);

app.set('view engine', 'ejs');
app.set('views', path.join(__dirname, 'views'));
app.use(express.static(path.join(__dirname, 'public')));

app.use((req, res, next) => {
  console.log(req.session);
  next();
});

app.use('/api/auth', authRoutes);
app.use('/api/files', fileRoutes);
app.use('/preview', previewRoutes);

app.get('/', (req, res) => {
  if (req.session.userId) {
    res.render('dashboard/index');
  } else {
    res.render('auth/login');
  }
});

app.get('/register', (req, res) => {
  if (req.session.userId) {
    return res.redirect('/');
  }
  res.render('auth/register');
});

app.get('/login', (req, res) => {
  if (req.session.userId) {
    return res.redirect('/');
  }
  res.render('auth/login');
});

app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ message: 'Server error', error: err.message });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});
