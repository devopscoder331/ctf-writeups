#!/bin/bash

/bin/qemu-system-x86_64 \
    -m 4G \
    -cpu host \
    -enable-kvm \
    -nographic \
    -snapshot \
    -hda ./ponkrnl.qcow2 \
    -netdev user,id=net0,hostfwd=tcp::1337-:31337 \
    -device rtl8139,netdev=net0 \
    -monitor stdio \
    -serial none \
    -drive if=pflash,format=raw,file="./OVMF.4m.fd",readonly=on