import puppeteer from 'puppeteer';
import hbs from 'handlebars';
import { v4 as uuidv4 } from 'uuid';
import fs from 'fs';
import path from 'path';


class BrowserHandler {
    constructor() {
        const launch_browser = async () => {
            this.browser = false;
            this.browser = await puppeteer.launch({
                product: 'chrome',
                args: ['--no-sandbox', '--disable-setuid-sandbox'],
                headless: "new",
            });
            this.browser.on('disconnected', launch_browser);
        };

        (async () => {
            await launch_browser();
        })();
    }
}

const wait_for_browser = browser_handler => new Promise((resolve, reject) => {
    const browser_check = setInterval(() => {
        if (browser_handler.browser !== false) {
            clearInterval(browser_check);
            resolve(true);
        }
    }, 100);
});

const browser_handler = new BrowserHandler;

hbs.registerHelper("trace", function(traceId) {
    if (traceId && typeof traceId === 'string' && traceId.length === 36) {
        return new hbs.SafeString(traceId)
    } else {
        return new hbs.SafeString(uuidv4())
    }
});

export async function generateBoardingPass(boardingPassData) {
    
    const filePath = path.join(path.resolve(''), 'ticket.tmpl');
    const template = hbs.compile(fs.readFileSync(filePath, 'utf8'));
    const boardingPassTemplate = template(boardingPassData);

    const ticketPath = path.join(path.resolve(''), `tickets/boarding-pass-${uuidv4()}.html`);
    fs.writeFileSync(ticketPath, boardingPassTemplate);

    const durationLabel = `[${boardingPassData.traceId}] - Pdf generation duration`;
    console.time(durationLabel);
    await wait_for_browser(browser_handler);

    const page = await browser_handler.browser.newPage();
    page.setDefaultNavigationTimeout(3_000);
    await page.setJavaScriptEnabled(false);


    try {
        await page.goto("file://"+ticketPath, {waitUntil: 'load'});
        await page.emulateMediaType("print");

        const boardingPassPDF = await page.pdf({
            format: 'A4',
            printBackground: true,
            margin: {
                top: "1cm",
                right: "1cm",
                bottom: "1cm",
                left: "1cm",
              },
        });
        return boardingPassPDF;
    } catch (error) {
        throw error;
    } finally {
        console.timeEnd(durationLabel);
        await page.close();
        fs.unlinkSync(ticketPath);
    }

}