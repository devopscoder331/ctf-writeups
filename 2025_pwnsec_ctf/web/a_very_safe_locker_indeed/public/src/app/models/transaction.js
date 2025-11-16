const mongoose = require('mongoose');

const transactionSchema = new mongoose.Schema({
  user_from: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  user_to: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  type: String, // e.g., 'transfer', 'deposit', 'withdraw'
  amount: Number,
  date: { type: Date, default: Date.now },
});

const Transaction = mongoose.model('Transaction', transactionSchema);
module.exports = Transaction;