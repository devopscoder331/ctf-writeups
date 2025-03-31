const mongoose = require('mongoose');
const crypto = require('crypto');

const FileSchema = new mongoose.Schema({
  filename: {
    type: String,
    required: true
  },
  originalName: {
    type: String,
    required: true
  },
  path: {
    type: String,
    required: true
  },
  size: {
    type: Number,
    required: true
  },
  mimetype: {
    type: String,
    required: true
  },
  user: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  parent: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Folder',
    default: null
  },
  isFolder: {
    type: Boolean,
    default: false
  },
  uploadedAt: {
    type: Date,
    default: Date.now
  },
  // Share link properties
  isShared: {
    type: Boolean,
    default: false
  },
  shareToken: {
    type: String,
    default: null
  },
  shareExpiry: {
    type: Date,
    default: null
  }
});

// Generate a unique share token
FileSchema.methods.generateShareToken = function(expiryHours = 24) {
  this.shareToken = crypto.randomBytes(20).toString('hex');
  
  // Set expiry date (default: 24 hours from now)
  const now = new Date();
  this.shareExpiry = new Date(now.getTime() + expiryHours * 60 * 60 * 1000);
  
  this.isShared = true;
  return this.shareToken;
};

// Remove share token
FileSchema.methods.revokeShare = function() {
  this.shareToken = null;
  this.shareExpiry = null;
  this.isShared = false;
};

// Check if share is valid
FileSchema.methods.isShareValid = function() {
  if (!this.isShared || !this.shareToken) {
    return false;
  }
  
  if (this.shareExpiry && new Date() > this.shareExpiry) {
    return false;
  }
  
  return true;
};

module.exports = mongoose.model('File', FileSchema); 