FROM node:latest

WORKDIR /app
#ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD true
# ^ use this for m1 mac docker
RUN  apt-get update \
     && apt-get install -y wget gnupg ca-certificates \
     && apt-get install -y chromium \ 
     && rm -rf /var/lib/apt/lists/* \
     && wget --quiet https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh -O /usr/sbin/wait-for-it.sh \
     && chmod +x /usr/sbin/wait-for-it.sh

RUN  ln -s /usr/bin/chromium /usr/bin/chromium-browser

COPY src/package.json .
RUN npm install
COPY src ./

CMD ["npm", "start"]