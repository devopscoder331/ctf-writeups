FROM ubuntu:latest

RUN apt update && \
    apt install -y \
    mysql-server \
    python3 \
    python3-pip \
    python3-dev \
    gunicorn \
    pkg-config \
    default-libmysqlclient-dev \
    build-essential \
    bsdmainutils \
    && apt clean \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /destroyer

# Install Python packages
COPY requirements.txt .
RUN pip install -r requirements.txt --break-system-packages 

# Copy MySQL configuration file
COPY my.cnf /etc/mysql/my.cnf
COPY init.sql /docker-entrypoint-initdb.d/

# Copy application code
COPY ./destroyer /destroyer
RUN chmod -R a+w /destroyer

# Expose the ports
EXPOSE 1337

# Add flag
RUN dir1=$(hexdump -vn16 -e'4/4 "%08x"' /dev/urandom); dir2=$(hexdump -vn16 -e'4/4 "%08x"' /dev/urandom); dir3=$(hexdump -vn16 -e'4/4 "%08x"' /dev/urandom); mkdir -p /tmp/$dir1/$dir2/$dir3; echo '< REDACTED >' > /tmp/$dir1/$dir2/$dir3/flag.txt


# Create a script to start MySQL and Python app
COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# Set the entrypoint
CMD ["docker-entrypoint.sh"]