#!/bin/sh

sudo -u nobody /qemu-system-x86_64 \
    -m 128M \
    -M q35 \
    -cpu kvm64,+smep,+smap \
    -kernel /task/kernel/bzImage \
    -initrd initramfs.cpio.gz \
    -nographic \
    -L /task/fs/pc-bios \
    -monitor /dev/null \
    -no-reboot \
    -append "console=ttyS0 kaslr panic=1"
