const express = require('express');
const fs = require('fs');
const app = express();
const port = 80;
const { launchBrowser } = require('./utils/browser');
const { visitPage } = require('./utils/browser');

const FLAG = process.env.FLAG || 'flag{example_flag_for_testing}';

app.use('/flag', (req, res, next) => {
  const clientIP = req.ip || req.connection.remoteAddress;

  if (clientIP !== '127.0.0.1' && clientIP !== '::1' && clientIP !== '::ffff:127.0.0.1') {
    console.log(`[!] Forbidden access from ${clientIP}`)
    return res.status(403).send('Forbidden: Invalid IP');
  }

  if (req.get('Origin')) {
    return res.status(403).send('Forbidden: Origin not allowed');
  }

  next();
});
app.use((req, res, next) => {
  // is it enough?
  res.setHeader("Access-Control-Allow-Origin", "https://net.net");
  res.setHeader("Access-Control-Allow-Credentials", "true");
  res.setHeader("Content-Security-Policy", "frame-ancestors 'none'");
  res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
  res.setHeader("Pragma", "no-cache");
  res.setHeader("Expires", "0");
  res.setHeader("X-Content-Type-Options", "nosniff");
  res.setHeader("Cross-Origin-Resource-Policy", "same-origin");
  res.setHeader("Cross-Origin-Opener-Policy", "same-origin");
  res.setHeader("Cross-Origin-Embedder-Policy", "require-corp");
  next();
});

app.get('/', (req, res) => {
  res.sendFile(__dirname + '/public/index.html');
});

app.get('/flag', (req, res) => {
  res.send(FLAG);
});

const wait = ms => new Promise(resolve => setTimeout(resolve, ms));

app.get('/report', async (req, res) => {
  const targetUrl = req.query.url;
  if (!targetUrl || !/^https?:\/\//.test(targetUrl)) {
    return res.status(400).send('Invalid URL');
  }

  res.send(`Bot will visit your report shortly`);

  (async () => {
    let browser;
    try {
      browser = await launchBrowser();
      const page = await browser.newPage();
      await visitPage(page, targetUrl);

      console.log(`[+] Bot is visiting ${targetUrl}...`);

      await wait(150000);

      console.log(`[+] Bot finished visiting ${targetUrl}`);

      await page.close();
      await browser.close();
    } catch (err) {
      console.error(`[-] Report bot error for ${targetUrl}:`, err);
      if (browser) await browser.close();
    }
  })();
});

app.listen(port, () => {
  console.log(`Server running on http://127.0.0.1:${port}`);
});