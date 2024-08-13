FROM ubuntu:22.04

RUN apt-get -y update --fix-missing
RUN apt-get -y install software-properties-common
RUN apt-get -y install python3
RUN apt-get -y install libicu-dev
RUN apt-get -y install libatomic1
RUN apt-get -y install libstdc++6
RUN apt-get -y install socat
RUN mkdir /test
RUN useradd -m ctf
RUN echo "ctf:ctf" | chpasswd
WORKDIR /home/ctf
ADD d8 /home/ctf
ADD server.py /home/ctf
ADD snapshot_blob.bin /home/ctf
ADD run.sh /home/ctf
ADD test /test
COPY flag /
COPY catflag /
RUN chown -R root:root /home/ctf
RUN chmod -R 555 /home/ctf

RUN chown 200000:200000 /flag && chmod 400 /flag
RUN chown 200000:ctf /catflag && chmod 755 /catflag && chmod u+s /catflag

#HEALTHCHECK --interval=10s --timeout=10s --retries=3 CMD nc 127.0.0.1 1337 || exit 1

USER ctf
EXPOSE 1337
CMD ./run.sh
