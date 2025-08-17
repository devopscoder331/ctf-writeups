FROM ubuntu@sha256:f995e05e8adc3292853cc37e6edda72351f8002ce7469a29322d19e01529cb9f

RUN apt-get update && apt-get dist-upgrade -y && apt-get install qemu-system-x86 -yyq
RUN apt-get install cpio socat python3 python3-pip -yyq

RUN pip3 install requests --break-system-packages

RUN mkdir -p /task/fs
RUN mkdir -p /task/kernel

COPY bzImage /task/kernel/
COPY compress.sh  decompress.sh rootfs.cpio.gz  run.sh /task/fs/
RUN chmod ugo+rx /task/fs/run.sh && chmod ugo-w /task/fs/run.sh
RUN chmod ugo+rx /task/fs/decompress.sh && chmod ugo-w /task/fs/decompress.sh
RUN chmod ugo+rx /task/fs/compress.sh && chmod ugo-w /task/fs/compress.sh
RUN chmod ugo+rx /task/fs/rootfs.cpio.gz && chmod ugo-w /task/fs/rootfs.cpio.gz
RUN chmod ugo+rx /task/kernel/bzImage && chmod ugo-w /task/kernel/bzImage

ENV  PYTHONUNBUFFERED true

WORKDIR /

CMD ["socat", "TCP-LISTEN:9000,reuseaddr,fork,keepalive", "EXEC:/task/fs/run.sh"]
