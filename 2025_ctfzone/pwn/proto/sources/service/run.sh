#!/bin/sh

qemu-system-x86_64 \
    -cpu Haswell-noTSX-IBRS,vmx=on \
    -enable-kvm \
    -m 256M \
    -smp 2 \
    -kernel /task/kernel/bzImage \
    -initrd /task/fs/rootfs.cpio.gz \
    -snapshot \
    -nographic \
    -monitor /dev/null \
    -no-reboot \
    -append "console=ttyS0 nokaslr kpti=1 quiet panic=1"
