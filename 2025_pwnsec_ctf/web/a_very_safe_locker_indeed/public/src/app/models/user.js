const mongoose = require('mongoose')

const userSchema = new mongoose.Schema({
  firstName: String,
  lastName: String,
  phoneNumber: String,
  email: String,
  password: String,
  searchHistory: [String],
  lockerBalance: { type: Number, default: 0 },
  mainBalance: { type: Number, default: 10 },
  isMaster: { type: Boolean, default: false },
  masterId: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
})

const User = mongoose.model('User', userSchema)
module.exports = User
