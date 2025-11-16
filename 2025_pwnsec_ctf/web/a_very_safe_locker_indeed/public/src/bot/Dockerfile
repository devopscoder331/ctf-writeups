FROM node:21 as deps
WORKDIR /app
COPY ./*.json ./
RUN npm install

FROM node:21

RUN apt-get update -y
RUN npx playwright install-deps

RUN adduser bot

USER bot
ARG BROWSER=chromium
ENV BROWSER ${BROWSER}

COPY --from=deps /app/node_modules /home/bot/node_modules
COPY . /home/bot/
USER bot

RUN mkdir -p /home/bot/.cache/ms-playwright/chromium-1140; cd /home/bot/.cache/ms-playwright/chromium-1140; wget -nc https://playwright.azureedge.net/builds/chromium/1140/chromium-linux.zip; unzip chromium-linux.zip 

CMD ["node", "/home/bot/index.js"]