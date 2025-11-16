const express = require('express');
const bodyParser = require('body-parser');
const crypto = require('crypto');
const path = require('path');
const fs = require('fs');
const multer = require('multer');
const puppeteer = require('puppeteer');

const app = express();
app.use(bodyParser.json());
app.use(express.static('static'));
app.use(express.static('public'));

const uploadDir = path.join(__dirname, 'static');
if (!fs.existsSync(uploadDir)) fs.mkdirSync(uploadDir, { recursive: true });

const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, uploadDir),
  filename: (req, file, cb) => {
    const rndHex = crypto.randomBytes(16).toString('hex');
    cb(null, rndHex);
  }
});
const upload = multer({ storage });

// Upload endpoint
app.post('/upload', upload.single('file'), (req, res) => {
  if (!req.file) {
    return res.status(400).json({ success: false, error: 'no file uploaded' });
  }
  const rndHex = req.file.filename;
  const filePath = path.join(uploadDir, rndHex);
  
  fs.chmodSync(filePath, 0o755);
  
  const fileUrl = `/${rndHex}`;
  console.log(`Uploaded file: ${rndHex}`);

  res.json({
    success: true,
    file: rndHex,
    url: fileUrl,
    message: 'file uploaded successfully'
  });
});

app.post('/report', async (req, res) => {
  const { url, username, issue , description } = req.body;
  reports = {};
  if (!username || !issue || !description) {
    return res.status(400).json({ success: false, error: 'username, issue, and description required' });
  }
  if (!reports[username]) reports[username] = {};
  reports[username][issue] = description;

  if (!url) {
    return res.status(400).json({ success: false, error: 'url required' });
  }

  let browser;
  try {
    browser = await puppeteer.launch({
      args: ['--no-sandbox', '--disable-setuid-sandbox']
    });
    const page = await browser.newPage();

    console.log(`[BOT] Visiting: ${url}`);
    await page.goto(url, { waitUntil: 'load', timeout: 15000 });

    res.json({ success: true, message: `bot visited ${url}` });
  } catch (err) {
    console.error('Bot error:', err);
    res.status(500).json({ success: false, error: 'bot failed to visit url' });
  } finally {
    if (browser) await browser.close().catch(() => {});
  }
});

// Serve frontend
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});
app.get('/report', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'report.html'));
});

process.on('SIGINT', () => {
  console.log('Shutting down...');
  process.exit();
});

const PORT = 3000;
app.listen(PORT, () => console.log(`Server running on http://localhost:${PORT}`));
