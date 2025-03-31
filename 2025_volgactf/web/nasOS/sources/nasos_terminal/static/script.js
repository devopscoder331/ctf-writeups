const textElement = document.getElementById("terminal-text");
const chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*";
let terminalLine = " ".repeat(40); 

function getRandomChar() {
    return chars[Math.floor(Math.random() * chars.length)];
}

function updateTerminal() {
    terminalLine = terminalLine.substring(1) + getRandomChar();
    textElement.textContent = terminalLine;
}

setInterval(updateTerminal, 100);
