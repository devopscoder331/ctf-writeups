const crypto = require("node:crypto");
const process = require('node:process');
const child_process = require('node:child_process');
const fs = require('node:fs');

const puppeteer = require("puppeteer");

const readline = require('readline').createInterface({
    input: process.stdin,
    output: process.stdout,
    terminal: false,
});
readline.ask = str => new Promise(resolve => readline.question(str, resolve));

const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

const TIMEOUT = process.env.TIMEOUT || 1000;
const TASK_URL = process.env.TASK_URL || 'http://ttest.com/';

const FLAG = process.env.FLAG || 'flag{dummy}';

const POW_BITS = process.env.POW_BITS || 0;

function makeid(length) {
    let result = '';
    const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    const charactersLength = characters.length;
    let counter = 0;
    while (counter < length) {
      result += characters.charAt(Math.floor(Math.random() * charactersLength));
      counter += 1;
    }
    return result;
}

async function pow() {
    const nonce = crypto.randomBytes(8).toString('hex');

    console.log('[*] Please solve PoW:');
    console.log(`hashcash -q -mb${POW_BITS} ${nonce}`);

    const answer = await readline.ask('> ');

    const check = child_process.spawnSync(
        '/usr/bin/hashcash',
        ['-q', '-f', './hashcash.sdb', `-cdb${POW_BITS}`, '-r', nonce, answer],
    );
    const correct = (check.status === 0);

    if (!correct) {
        console.log('[-] Incorrect.');
        process.exit(0);
    }

    console.log('[+] Correct.');
}

async function visit(url) {
    const params = {
        browser: 'chrome',
        args: [
            '--no-sandbox',
            '--disable-gpu',
            '--disable-extensions',
            '--js-flags=--jitless',
        ],
        headless: true,
    };

    const browser = await puppeteer.launch(params);
    const context = await browser.createBrowserContext();

    const pid = browser.process().pid;

    const shutdown = async () => {
        await context.close();
        await browser.close();

        try {
            process.kill(pid, 'SIGKILL');
        } catch(_) { }

        process.exit(0);
    };

    const page1 = await context.newPage();
    await page1.goto(TASK_URL);
    await sleep(1000);
    await page1.close();

    console.log('[+] Page 1 closed');

    const page2 = await context.newPage();
    await page2.goto(url);

    await sleep(TIMEOUT);
    await page2.close();

    console.log('[+] Page 2 closed');

    const LOGIN = makeid(16);
    const PASSWORD = makeid(16) + "!B0t";
    const NAME = makeid(8);

    const page3 = await context.newPage();
    console.log('[+] Page 3 opened');
    await page3.goto(`${TASK_URL}register`);

    await page3.waitForSelector('#register-form');
    await page3.type('#name', NAME);
    await page3.type('#email', `${LOGIN}@example.com`);
    await page3.type('#password', PASSWORD);
    await page3.click('.btn[type="submit"]');

    await sleep(300);

    await page3.goto(`${TASK_URL}`);
    await page3.waitForSelector('#upload-btn');
    await page3.waitForSelector('#upload-btn', { visible: true });

    await sleep(1000);

    const [fileChooser] = await Promise.all([
        page3.waitForFileChooser(),
        page3.click('#upload-btn'),
      ]);

    const tempFilePath = '/tmp/flag.txt';
    fs.writeFileSync(tempFilePath, FLAG);
    
    await fileChooser.accept([tempFilePath]);
    
    await page3.waitForFunction(() => {
      return !document.getElementById('file-upload-error').textContent;
    });
    
    await sleep(500);
    await page3.close();
}

async function main() {
    if (POW_BITS > 0) {
        await pow();
    }

    console.log('[?] Please input URL:');
    const url = await readline.ask('> ');

    if (!url.startsWith("http://") && !url.startsWith("https://")) {
        console.log('[-] Access denied.');
        process.exit(0);
    }

    console.log('[+] OK.');

    readline.close()

    await visit(url);

    await sleep(TIMEOUT);
}

main();
