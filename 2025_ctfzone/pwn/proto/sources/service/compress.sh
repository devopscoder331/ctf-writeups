#!/bin/bash

cd rootfs
find . | cpio -o -H newc -R root:root | gzip -9 > ../rootfs.cpio.gz
