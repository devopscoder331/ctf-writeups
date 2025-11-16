const express = require('express')
const app = express()
const path = require('path')
const route = express.Router()
const bot = require('./bot')
const rateLimit = require('express-rate-limit')

app.use(express.json())
app.use(express.urlencoded({ extended: false }))
app.set('view engine', 'ejs')
app.set('views', path.join(__dirname, 'views'))
if (process.env.USE_PROXY) {
  app.set('trust proxy', () => true)
}

const limit = rateLimit({
  ...bot,
  handler: (req, res, _next) => {
    const timeRemaining = Math.ceil(
      (req.rateLimit.resetTime - Date.now()) / 1000
    )
    res.status(429).json({
      error: `Too many requests, please try again later after ${timeRemaining} seconds.`,
    })
  },
})

route.post('/', limit, async (req, res) => {
  const { email, amount, userMessage } = req.body

  if (await bot.bot(email, amount, userMessage)) {
    return res.json({ success: true })
  } else {
    return res.json({ success: false })
  }
})

route.get('/', (_, res) => {
  const { name } = bot

  res.render('index', { name })
})

route.get('/healthz', (req, res) => {
  res.status(200).json({ status: 'OK' })
})

app.use('/', route)

app.listen(80, () => {
  console.log('Server running at http://localhost:80')
})
