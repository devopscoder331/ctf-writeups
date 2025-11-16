const express = require('express')
const Transaction = require('../models/transaction')
const User = require('../models/user')
const SearchHistory = require('../models/searchHistory')
const router = express.Router()

router.get('/transactions', async (req, res) => {
  if (!req.user) {
    return res.redirect('/login')
  }

  const userId = req.user.id

  const transactions = await Transaction.find({
    $or: [{ user_from: userId }, { user_to: userId }],
  })
    .sort({ date: -1 })
    .populate('user_from user_to')

  res.render('transactions', { transactions, user: req.user, nonce: req.nonce })
})

router.get('/transactions/search', async (req, res) => {
  if (!req.user) {
    return res.redirect('/login')
  }

  const searchTerm = req.query.searchTerm
  const userId = req.user.id

  if (!searchTerm) {
    return res.status(500).send('An error occured, please try again later.')
  }

  const searchRecord = new SearchHistory({ userId, searchTerm })
  await searchRecord.save()

  Transaction.find({
    $or: [
      { _id: searchTerm },
      { user_to: searchTerm },
      { user_from: searchTerm },
    ],
  })
    .populate('user_from user_to')
    .then((transactions) => {
      res.render('transactions', {
        transactions,
        user: req.user,
        nonce: req.nonce,
      })
    })
    .catch((err) => {
      res.render('transactions', {
        transactions: [],
        user: req.user,
        nonce: req.nonce,
      })
    })
})

router.post('/transfer', async (req, res) => {
  if (!req.user) {
    return res.redirect('/login')
  }

  let { receiverInfo, amount } = req.body
  amount = parseFloat(amount)

  if (!receiverInfo || amount == undefined) {
    return res.status(500).send('An error occured, please try again later.')
  }

  const user_from = await User.findById(req.user.id)

  if (isNaN(amount) || !Number.isFinite(amount)) {
    return res.render('index', {
      user: req.user,
      balance: user_from.mainBalance,
      failMessage: 'Please enter a valid amount to transfer.',
    })
  }

  let user_to = {}

  try {
    if (!receiverInfo.includes('@') && receiverInfo.trim().length == 24) {
      user_to = await User.findOne({ _id: receiverInfo })
    } else {
      user_to = await User.findOne({ email: receiverInfo })
    }
  } catch (error) {
    return res
      .status(500)
      .send('An error occurred while searching for the recipient.')
  }

  if (!user_to) {
    const fee_proportion = 100 * (amount / user_from.mainBalance) // Fee is proportional to the amount attempted to be sent
    user_from.mainBalance -= fee_proportion
    await user_from.save()

    return res.render('index', {
      user: req.user,
      balance: user_from.mainBalance,
      failMessage:
        'Failed to find the recipient user, a percentage fee has been applied to your account for this failed transaction.',
    })
  }

  if (user_to._id.toString() === req.user.id) {
    return res.render('index', {
      user: req.user,
      balance: user_from.mainBalance,
      failMessage: 'You cannot transfer funds to yourself.',
    })
  }

  const transaction_amount = parseFloat(amount) * 1.01 // Include 1% fee
  if (user_from.mainBalance >= transaction_amount && transaction_amount > 0) {
    user_from.mainBalance -= transaction_amount
    await user_from.save()
  } else {
    return res.render('index', {
      user: req.user,
      balance: user_from.mainBalance,
      failMessage: 'Insufficient balance to cover the transfer and fees.',
    })
  }

  user_to.mainBalance += parseFloat(amount)
  await user_to.save()

  const transaction = new Transaction({
    user_from: req.user.id,
    user_to: user_to._id,
    type: 'transfer',
    amount: transaction_amount,
  })
  await transaction.save()

  return res.render('index', {
    user: req.user,
    balance: user_from.mainBalance,
    successMessage:
      'Transfer successful go to <a href="/transactions">Transactions</a> to view details.',
  })
})

router.get('/api/search-history/autocomplete/:userId', async (req, res) => {
  if (!req.user) {
    return res.redirect('/login')
  }

  const userId = req.params.userId
  const limit = req.query.limit || 10

  await User.findById(userId)
    .then(async (user) => {
      if (
        !user ||
        (user._id.toString() !== req.user.id &&
          req.user.lastName !== `${user.lastName}'s Bank Master`)
      ) {
        // Only allow current user and his bankmaster to view the search history
        return res.status(403).send('Unauthorized')
      }

      return await SearchHistory.find({ userId })
        .limit(limit)
        .sort({ date: -1 })
        .then((pastSearches) => {
          const searchTerms = pastSearches.map((search) => search.searchTerm)
          res.json(searchTerms)
        })
    })
    .catch((err) => {
      return res.status(500).send('An error occured, please try again later.')
    })
})

module.exports = router
