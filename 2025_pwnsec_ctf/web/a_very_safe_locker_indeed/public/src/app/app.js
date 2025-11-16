const express = require('express')
const mongoose = require('mongoose')
const cookieParser = require('cookie-parser')
const { expressCspHeader, SELF, NONCE, NONE } = require('express-csp-header')
const path = require('path')

const authRoutes = require('./routes/auth')
const indexRoutes = require('./routes/index')
const transactionsRoutes = require('./routes/transactions')
const lockerRoutes = require('./routes/locker')
const masterRoutes = require('./routes/master')

const app = express()

// Connect to MongoDB
mongoose
  .connect(process.env.MONGODB_URI, {
    dbName: 'e-banking-app',
  })
  .catch((error) => console.log(error))
mongoose.connection.on('connected', () => {
  console.log('Connected to MongoDB')
})

// Apply CSP
app.use(
  expressCspHeader({
    directives: {
      'default-src': [SELF],
      'script-src': [SELF, NONCE],
      'base-uri': [NONE],
      'object-src': [NONE],
    },
  })
)

// Setup middlewares
app.use(express.urlencoded({ extended: true }))
app.use(cookieParser(process.env.COOKIE_PARSER_SECRET))
app.use(express.static('public'))
app.set('views', path.join(__dirname, 'views'))

// Pug setup
app.set('view engine', 'pug')

// Middleware to check user session
app.use((req, res, next) => {
  if (req.signedCookies.session) {
    req.user = {
      id: req.signedCookies.session.userId,
      firstName: req.signedCookies.session.firstName,
      lastName: req.signedCookies.session.lastName,
      phoneNumber: req.signedCookies.session.phoneNumber,
      email: req.signedCookies.session.email,
    }
  }
  next()
})

// Routes
app.use('/', authRoutes)
app.use('/', indexRoutes)
app.use('/', transactionsRoutes)
app.use('/', lockerRoutes)
app.use('/', masterRoutes)

// Health check route
app.get('/healthz', (req, res) => {
  res.status(200).json({ status: 'OK' })
})

// Start the server
app.listen(process.env.PORT, () => {
  console.log('Server is running on port ' + process.env.PORT)
})
