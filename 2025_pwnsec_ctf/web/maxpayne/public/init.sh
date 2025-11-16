#!/bin/bash


# Set up flag - use FLAG environment variable from platform
if [ -z "$FLAG" ]; then
    export FLAG="flag{DEFAULT}"
    echo "WARNING: Using default flag (FLAG env var not set)"
else
    echo "Flag loaded from environment"
fi

echo ""
echo "Setting up SQLite database..."
mkdir -p /var/www/html
chown -R www-data:www-data /var/www/html

echo "SQLite database path: $DB_PATH"
echo "Database will be initialized on first PHP request"

echo ""
echo "======================================"
echo "Starting All Services with Supervisor"
echo "======================================"
echo "Services to start:"
echo "- Apache (PHP Backend API): ${APP_URL}"
echo "- Flask Frontend: http://localhost:${FLASK_PORT}"
echo "- XSS Bot: ${BOT_URL}"
echo "- Traefik Proxy: http://localhost:7598"
echo "- SQLite Database: ${DB_PATH}"
echo ""
echo "Admin credentials: ${ADMIN_USERNAME}/${ADMIN_PASSWORD}"
echo "======================================"

exec /usr/bin/supervisord -c /etc/supervisor/conf.d/supervisord.conf
