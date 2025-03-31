FROM node:20-slim

RUN apt-get update && \
    apt-get install -y \
    socat hashcash curl gnupg2 wget \
    && rm -rf /var/lib/apt/lists/*

# Install Chrome dependencies
RUN apt-get update && \
    apt-get install -yq --no-install-recommends \
    gconf-service libasound2 libatk1.0-0 libc6 libcairo2 libcups2 libdbus-1-3 \
    libexpat1 libfontconfig1 libgbm1 libgcc1 libgconf-2-4 libgdk-pixbuf2.0-0 \
    libglib2.0-0 libgtk-3-0 libnspr4 libpango-1.0-0 libpangocairo-1.0-0 \
    libstdc++6 libx11-6 libx11-xcb1 libxcb1 libxcomposite1 libxcursor1 \
    libxdamage1 libxext6 libxfixes3 libxi6 libxrandr2 libxrender1 libxss1 \
    libxtst6 ca-certificates fonts-liberation libnss3 lsb-release xdg-utils \
    && rm -rf /var/lib/apt/lists/*

# Install Chrome
RUN wget -q https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
    apt-get update && \
    apt-get install -y ./google-chrome-stable_current_amd64.deb && \
    rm google-chrome-stable_current_amd64.deb && \
    rm -rf /var/lib/apt/lists/*

# Tell Puppeteer to use the installed Chrome instead of downloading its own
ENV PUPPETEER_EXECUTABLE_PATH=/usr/bin/google-chrome-stable
ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=true

COPY package.json .
RUN npm install

COPY . .
RUN chmod +x /entrypoint.sh

CMD ["/entrypoint.sh"]