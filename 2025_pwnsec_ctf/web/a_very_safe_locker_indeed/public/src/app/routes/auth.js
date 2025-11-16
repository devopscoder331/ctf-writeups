const express = require('express')
const User = require('../models/user')
const router = express.Router()

router.get('/register', (req, res) => {
  if (req.user) {
    return res.redirect('/')
  }

  res.render('register')
})

router.post('/register', async (req, res) => {
  const { firstName, lastName, phoneNumber, email, password } = req.body

  // Automatically create a master for the user
  const master = new User({
    firstName,
    lastName: `${lastName}'s Bank Master`,
    phoneNumber,
    email: `${email}.bankmaster`,
    password: process.env.BANK_MASTER_PASSWORD,
    isMaster: true,
  })
  await master.save()

  const user = new User({
    firstName,
    lastName,
    phoneNumber,
    email,
    password,
    isMaster: false,
    masterId: master._id,
  })
  await user.save()

  res.cookie(
    'session',
    { userId: user._id, firstName, lastName, phoneNumber, email },
    { signed: true, httpOnly: true, maxAge: 24 * 60 * 60 * 1000 }
  )

  res.redirect('/')
})

router.get('/login', (req, res) => {
  if (req.user) {
    return res.redirect('/')
  }

  res.render('login', { error: null })
})

router.post('/login', async (req, res) => {
  const { email, password } = req.body

  const user = await User.findOne({ email, password })
  if (!user) {
    return res.render('login', { error: 'Invalid email or password' })
  }

  res.cookie(
    'session',
    {
      userId: user._id,
      firstName: user.firstName,
      lastName: user.lastName,
      phoneNumber: user.phoneNumber,
      email: user.email,
    },
    {
      httpOnly: true,
      signed: true,
      maxAge: 1000 * 60 * 60 * 24,
    }
  )

  res.redirect('/')
})

router.get('/profile', (req, res) => {
  if (!req.user) {
    return res.redirect('/login')
  }

  res.render('profile', {
    user: req.user,
    successMessage: null,
    errorMessage: null,
  })
})

router.post('/profile', async (req, res) => {
  if (!req.user) {
    return res.redirect('/login')
  }

  const { firstName, lastName, phoneNumber, email } = req.body

  if (!firstName || !lastName || !phoneNumber || !email) {
    return res.render('profile', {
      user: req.user,
      successMessage: null,
      errorMessage: 'All fields are required.',
    })
  }

  try {
    const user = await User.findById(req.user.id)

    if (user.isMaster) {
      return res.render('profile', {
        user: req.user,
        successMessage: null,
        errorMessage: 'Bank masters cannot edit their profile.',
      })
    }

    user.firstName = firstName
    user.lastName = lastName
    user.phoneNumber = phoneNumber
    user.email = email
    await user.save()

    // Update the session cookie with new data
    res.cookie(
      'session',
      {
        userId: user._id,
        firstName: user.firstName,
        lastName: user.lastName,
        phoneNumber: user.phoneNumber,
        email: user.email,
      },
      {
        httpOnly: true,
        signed: true,
        maxAge: 1000 * 60 * 60 * 24,
      }
    )

    res.render('profile', {
      user: req.user,
      successMessage: 'Profile updated successfully.',
      errorMessage: null,
    })
  } catch (error) {
    console.error(error)
    res.render('profile', {
      user: req.user,
      successMessage: null,
      errorMessage: 'An error occurred while updating your profile.',
    })
  }
})

router.get('/logout', (req, res) => {
  res.clearCookie('session')
  res.redirect('/')
})

module.exports = router
