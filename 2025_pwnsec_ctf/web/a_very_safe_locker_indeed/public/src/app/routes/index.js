const express = require('express')
const User = require('../models/user')
const router = express.Router()

router.get('/', (req, res) => {
  if (req.user) {
    User.findById(req.user.id)
      .then((user) => {
        res.render('index', { user: req.user, balance: user.mainBalance })
      })
      .catch((err) => {
        res.redirect('/logout')
      })
  } else {
    res.render('index')
  }
})

module.exports = router
