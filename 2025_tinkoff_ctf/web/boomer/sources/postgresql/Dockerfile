FROM postgres:15

RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    netcat-openbsd \
    postgresql-plpython3-15 \
    procps \
    iputils-ping \
    net-tools \
    --no-install-recommends && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /tmp/python-cache && \
    chmod 777 /tmp/python-cache

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONPYCACHEPREFIX=/tmp/python-cache

RUN mkdir -p /run/secrets && \
    openssl rand -base64 32 > /run/secrets/db_password && \
    chmod 644 /run/secrets/db_password && \
    echo "Generated random password:"

ENV POSTGRES_PASSWORD_FILE=/run/secrets/db_password

COPY postgresql.conf /etc/postgresql/postgresql.conf
COPY pg_hba.conf /etc/postgresql/pg_hba.conf
COPY init-db.sql /docker-entrypoint-initdb.d/

RUN mkdir -p /var/run/postgresql && \
    chmod 777 /var/run/postgresql

CMD ["postgres", "-c", "config_file=/etc/postgresql/postgresql.conf"] 
