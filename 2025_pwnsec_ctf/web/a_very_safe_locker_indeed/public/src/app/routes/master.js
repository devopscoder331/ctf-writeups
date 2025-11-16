const express = require('express')
const axios = require('axios')
const User = require('../models/user')
const Transaction = require('../models/transaction')
const router = express.Router()

router.get('/master', async (req, res) => {
  if (!req.user) {
    return res.redirect('/login')
  }

  const client = await User.findById(req.user.id)

  if (client.mainBalance < 1_000_000_000_000) {
    return res.redirect('/locker')
  }

  res.render('master', {
    user: req.user,
    clientEmail: client.email,
    successMessage: req.query.successMessage
      ? 'You bank master has successfully deposited the amount in your locker.'
      : false,
    failMessage: req.query.failMessage
      ? 'Something went wrong while the bank master was depositing your amount, please try again later.'
      : false,
  })
})

router.post('/master', async (req, res) => {
  if (!req.user) {
    return res.redirect('/login')
  }

  const { email, amount, userMessage } = req.body

  axios
    .post('http://bot', { email, amount, userMessage })
    .then((response) => {
      data = response.data

      return res.render('master', {
        user: req.user,
        clientEmail: email,
        successMessage: data.success
          ? 'You bank master has successfully deposited the amount in your locker.'
          : false,
        failMessage: !data.success
          ? 'Something went wrong while the bank master was depositing your amount, please try again later.'
          : false,
      })
    })
    .catch((error) => {
      return res.status(404).send('An unexpected error occured')
    })
})

router.post('/master/deposit', async (req, res) => {
  if (!req.user) {
    return res.redirect('/login')
  }

  const { amount } = req.body

  const master = await User.findById(req.user.id)

  if (!master.isMaster) {
    return res.status(403).send('<h2>Unauthorized</h2>')
  }

  const client = await User.findOne({ masterId: req.user.id })

  const user_to = client._id
  const user_from = req.user.id

  const transaction = new Transaction({
    user_from,
    user_to,
    type: 'locker deposit',
    amount: parseFloat(amount),
  })

  await transaction.save()

  // Simulate the master depositing to the user's locker
  client.lockerBalance += parseFloat(amount)
  client.mainBalance -= parseFloat(amount)
  await client.save()

  res.redirect('/locker')
})

router.get('/master/confedential', async (req, res) => {
  if (!req.user) {
    return res.redirect('/login')
  }

  const userId = req.user.id
  const user = await User.findById(userId)

  if (!user.isMaster) {
    return res.status(403).send('<h2>Unauthorized</h2>')
  }

  res.send(process.env.FLAG)
})

module.exports = router
