const express = require('express')
const User = require('../models/user')
const Transaction = require('../models/transaction')
const { nonce } = require('express-csp-header')
const router = express.Router()

router.get('/locker', async (req, res) => {
  if (!req.user) {
    return res.redirect('/login')
  }

  const userId = req.user.id
  const user = await User.findById(userId)

  if (user.isMaster) {
    const client = await User.findOne({ masterId: userId })

    if (client.mainBalance < 1_000_000_000_000) {
      return res.render('locker', {
        unauthorized: true,
        user: req.user,
      })
    }

    const userMessage = req.query.userMessage || ''

    res.render('locker', {
      lockerBalance: client.lockerBalance,
      user: req.user,
      isMaster: true,
      nonce: req.nonce,
      userMessage: userMessage
        .replace(/"/g, '\\"')
        .replace(/'/g, "\\'")
        .replace(/\n/g, '\\n')
        .replace(/>/g, '&gt;')
        .replace(/</g, '&lt;'),
    })
  } else if (user.mainBalance < 1_000_000_000_000) {
    return res.render('locker', {
      unauthorized: true,
      user: req.user,
    })
  } else {
    res.render('locker', {
      lockerBalance: user.lockerBalance,
      user: req.user,
    })
  }
})

router.post('/locker/withdraw', (req, res) => {
  if (!req.user) {
    return res.redirect('/login')
  }

  const userId = req.user.id
  const amount = req.body.withdraw_amount

  User.findById(userId)
    .then(async (user) => {
      if (user.mainBalance < 1_000_000_000_000) {
        return res.render('locker', {
          unauthorized: true,
          user: req.user,
        })
      }

      if (amount > user.lockerBalance) {
        return res.render('locker', {
          lockerBalance: user.lockerBalance,
          user: req.user,
          failMessage: "You don't have enough balance in your locker.",
        })
      }

      if (amount <= 0 || isNaN(amount)) {
        return res.render('locker', {
          lockerBalance: user.lockerBalance,
          user: req.user,
          failMessage: 'Please enter a valid amount to withdraw.',
        })
      }

      user.lockerBalance -= parseFloat(amount)
      user.mainBalance += parseFloat(amount)

      const user_to = userId
      const user_from = userId

      const transaction = new Transaction({
        user_from,
        user_to,
        type: 'locker withdraw',
        amount: parseFloat(amount),
      })

      await transaction.save()

      user
        .save()
        .then((user) => {
          res.render('locker', {
            lockerBalance: user.lockerBalance,
            user: req.user,
            successMessage: 'Withdrawn successfully.',
          })
        })
        .catch((err) => {
          res.render('locker', {
            lockerBalance: user.lockerBalance,
            user: req.user,
            failMessage:
              'Something went wrong while withdrawing, try again later.',
          })
        })
    })
    .catch((err) => {
      res
        .status(500)
        .send(
          'An error happened, please try again later or contact customer service'
        )
    })
})

module.exports = router
