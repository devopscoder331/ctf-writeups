const mongoose = require('mongoose');

const searchHistorySchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
  searchTerm: { type: String, required: true },
  date: { type: Date, default: Date.now },
});

const SearchHistory = mongoose.model('SearchHistory', searchHistorySchema);
module.exports = SearchHistory;
