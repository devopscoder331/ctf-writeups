FROM ubuntu@sha256:5487c53773e2a8576213d1a2e18148fe167fabecfe7844724792f63041190d9d

RUN apt-get -y update
RUN apt-get -y install socat

RUN useradd -m ctf
RUN mkdir /app
WORKDIR /app

COPY ./ /app
RUN chmod +x ./run.sh

USER ctf

EXPOSE 15557
CMD ./run.sh

