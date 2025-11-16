FROM ubuntu:24.04

RUN apt-get update -y
RUN apt install -y \
    build-essential \
    pkg-config \
    gdb \
    lsb-release \
    make \
    wget \
    libffi-dev \
    libbz2-dev \
    liblzma-dev \
    libssl-dev \
    zlib1g-dev \
    libncurses5-dev \
    libncursesw5-dev \
    libreadline-dev \
    libsqlite3-dev \
    libgdbm-dev \
    libgdbm-compat-dev \
    libdb-dev \
    libexpat1-dev \
    uuid-dev \
    libxml2-dev \
    libxmlsec1-dev \
    tk-dev \
    libbluetooth-dev \
    libnss3-dev \
    libedit-dev \
    libcap-dev \
    libpam0g-dev \
    socat \
    xz-utils \
    tar \
    curl \
 && rm -rf /var/lib/apt/lists/*

RUN mkdir /application
WORKDIR /application

# build/install Python (you may prefer using system python or deadsnakes PPA)
RUN wget https://www.python.org/ftp/python/3.12.3/Python-3.12.3.tar.xz -O Python.tar.xz && \
    xz -d Python.tar.xz && \
    tar xf Python.tar && \
    cd Python-3.12.3 && ./configure && \
    make -j$(nproc) && \
    make install && \
    cd .. && rm -rf Python-3.12.3 Python.tar


WORKDIR /application/app
COPY source/requirements.txt source/start.sh ./

# upgrade pip then install requirements
RUN pip3 install --upgrade pip setuptools wheel && \
    pip3 install -r requirements.txt --break-system-packages

COPY source/ .

ENV FLASK_APP=app.py

EXPOSE 5000
CMD ["./start.sh"]
