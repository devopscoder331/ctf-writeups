const multer = require('multer');
const path = require('path');
const fs = require('fs');

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const userId = req.session.userId;
    const uploadPath = path.join(__dirname, '../public/uploads', userId.toString());
    if (!fs.existsSync(uploadPath)) {
      fs.mkdirSync(uploadPath, { recursive: true });
    }
    cb(null, uploadPath);
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    const ext = path.extname(file.originalname);
    cb(null, uniqueSuffix + ext);
  }
});

const fileFilter = (req, file, cb) => {
  cb(null, true);
};

const limits = {
  fileSize: 50 * 1024 * 1024,
};

const upload = multer({ 
  storage, 
  fileFilter, 
  limits 
});

module.exports = upload; 