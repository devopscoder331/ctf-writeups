FROM php:apache

# Set environment variables to avoid interactive prompts
ENV DEBIAN_FRONTEND=noninteractive
ENV TZ=UTC

# Install system dependencies (Apache, PHP, SQLite, Python, Chrome for Playwright)
RUN set -eux; \
    echo '#!/bin/sh' > /usr/sbin/policy-rc.d; \
    echo 'exit 0' >> /usr/sbin/policy-rc.d; \
    chmod +x /usr/sbin/policy-rc.d; \
    apt-get update && apt-get install -y --no-install-recommends \
        sqlite3 \
        curl \
        wget \
        gnupg \
        ca-certificates \
        fonts-liberation \
        libappindicator3-1 \
        libasound2 \
        libatk-bridge2.0-0 \
        libatk1.0-0 \
        libcups2 \
        libdrm2 \
        libgtk-3-0 \
        libnspr4 \
        libnss3 \
        libx11-xcb1 \
        libxcomposite1 \
        libxdamage1 \
        libxrandr2 \
        xdg-utils \
        libgbm1 \
        libxss1 \
        supervisor \
        python3 \
        python3-pip \
        python3-venv \
    && rm -rf /var/lib/apt/lists/*

# Download and install Traefik
RUN wget https://github.com/traefik/traefik/releases/download/v3.0.0/traefik_v3.0.0_linux_amd64.tar.gz && \
    tar -xzf traefik_v3.0.0_linux_amd64.tar.gz && \
    mv traefik /usr/local/bin/traefik && \
    chmod +x /usr/local/bin/traefik && \
    rm traefik_v3.0.0_linux_amd64.tar.gz

# Enable Apache modules and install PHP extensions
RUN a2enmod rewrite
RUN docker-php-ext-install pdo

# Environment variables for all components
ENV PLAYWRIGHT_BROWSERS_PATH=/opt/playwright
ENV ADMIN_USERNAME=adminnnnnnn
ENV ADMIN_PASSWORD=adminnnnnnn
ENV APP_URL=http://localhost:80
ENV BOT_URL=http://localhost:3000
ENV FLASK_URL=http://localhost:5000
ENV DB_PATH=/var/www/html/database.sqlite

# Bot configuration environment variables
ENV APPNAME=Admin
ENV APPURL=http://localhost:5000
ENV APPURLREGEX=^http://localhost:5000/view_note/
ENV APPLIMITTIME=60
ENV APPLIMIT=5

# Flask configuration environment variables
ENV FLASK_HOST=0.0.0.0
ENV FLASK_PORT=5000
ENV FLASK_DEBUG=false
ENV PHP_BACKEND_URL=http://localhost:80

# Create application directories
RUN mkdir -p /app/bot /app/frontend
WORKDIR /app

# Install Playwright and dependencies
WORKDIR /app/bot
ENV PLAYWRIGHT_BROWSERS_PATH=/opt/playwright
RUN pip install --break-system-packages playwright && \
    playwright install --with-deps chromium

# Copy bot files
COPY Bot/bot/ /app/bot/

# Copy and install Python Flask frontend dependencies
WORKDIR /app/frontend
COPY Frontend/requirements.txt /app/frontend/
RUN python3 -m pip install -r requirements.txt --break-system-packages

# Copy Flask frontend files
COPY Frontend/ /app/frontend/

# Create traefik directory and copy config
RUN mkdir -p /traefik
COPY Frontend/traefik/config.toml /traefik/config.toml

# Copy PHP backend files
COPY *.php /var/www/html/
COPY database.sqlite.sql /var/www/html/

# Copy init script
COPY init.sh /app/init.sh
RUN chmod +x /app/init.sh

# Create supervisor configuration for managing multiple processes
RUN echo '[supervisord]\n\
nodaemon=true\n\
user=root\n\
\n\
[program:apache]\n\
command=/usr/sbin/apache2ctl -DFOREGROUND\n\
stdout_logfile=/dev/stdout\n\
stdout_logfile_maxbytes=0\n\
stderr_logfile=/dev/stderr\n\
stderr_logfile_maxbytes=0\n\
autorestart=true\n\
\n\
[program:bot]\n\
command=python3 /app/bot/app.py\n\
directory=/app/bot\n\
stdout_logfile=/dev/stdout\n\
stdout_logfile_maxbytes=0\n\
stderr_logfile=/dev/stderr\n\
stderr_logfile_maxbytes=0\n\
autorestart=true\n\
\n\
[program:flask]\n\
command=python3 /app/frontend/app.py\n\
directory=/app/frontend\n\
stdout_logfile=/dev/stdout\n\
stdout_logfile_maxbytes=0\n\
stderr_logfile=/dev/stderr\n\
stderr_logfile_maxbytes=0\n\
autorestart=true\n\
\n\
[program:traefik]\n\
command=traefik --configFile=/traefik/config.toml\n\
directory=/app\n\
stdout_logfile=/dev/stdout\n\
stdout_logfile_maxbytes=0\n\
stderr_logfile=/dev/stderr\n\
stderr_logfile_maxbytes=0\n\
autorestart=true' > /etc/supervisor/conf.d/supervisord.conf

# Expose port for Traefik proxy
EXPOSE 7598

# Set working directory back to app
WORKDIR /app

# Set entrypoint
ENTRYPOINT ["/app/init.sh"]
