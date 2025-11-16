FROM node:20-bullseye

WORKDIR /app

COPY package*.json ./

RUN npm install
RUN npm install express
RUN npm install puppeteer

RUN apt-get update && \
    apt-get install -y firefox-esr curl && \
    rm -rf /var/lib/apt/lists/*

COPY . .
ENV FLAG="flag{example_flag_for_testing}"

EXPOSE 80

CMD ["node", "app.js"]