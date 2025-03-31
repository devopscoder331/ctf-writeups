FROM ubuntu:latest

RUN apt-get update && apt-get dist-upgrade -y && apt-get install qemu-system-x86 sudo -yyq
RUN apt-get install cpio socat python3 python3-pip libcurl4-openssl-dev -yyq

RUN pip3 install requests --break-system-packages

RUN mkdir -p /task/fs
RUN mkdir -p /task/fs/pc-bios
RUN mkdir -p /task/kernel

COPY bzImage /task/kernel/
COPY compress.sh  decompress.sh initramfs.cpio.gz  run.sh /task/fs/
COPY qemu-system-x86_64 /
COPY pc-bios /task/fs/pc-bios
COPY flag /
RUN chmod ugo+rx /flag && chmod ugo-w /flag
RUN chmod ugo+rx /qemu-system-x86_64 && chmod ugo-w /qemu-system-x86_64
RUN chmod ugo+rx /task/fs/run.sh && chmod ugo-w /task/fs/run.sh
RUN chmod ugo+rx /task/fs/decompress.sh && chmod ugo-w /task/fs/decompress.sh
RUN chmod ugo+rx /task/fs/compress.sh && chmod ugo-w /task/fs/compress.sh
RUN chmod ugo+rx /task/fs/initramfs.cpio.gz && chmod ugo-w /task/fs/initramfs.cpio.gz
RUN chmod ugo+rx /task/kernel/bzImage && chmod ugo-w /task/kernel/bzImage

ENV  PYTHONUNBUFFERED true

WORKDIR /task/fs

CMD ["socat", "TCP-LISTEN:9000,reuseaddr,fork,keepalive", "EXEC:./run.sh"]
