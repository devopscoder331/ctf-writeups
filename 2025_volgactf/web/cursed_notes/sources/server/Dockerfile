FROM fedora:latest

WORKDIR /app

RUN dnf -y update && dnf install -y \
    python3 \
    python3-pip \
    glib2 \
    pango \
    postgresql-devel \
    gcc \
    nc \
    postgresql \
    su \
    && dnf clean all

ENV PATH=$PATH:/usr/pgsql-13/bin

COPY requirements.txt .
RUN pip3 install --no-cache-dir -r requirements.txt

COPY . .

RUN useradd user

RUN chmod +x entrypoint.sh

EXPOSE 8000

CMD ["./entrypoint.sh"]
