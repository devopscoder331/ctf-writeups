#!/bin/sh
socat tcp-listen:13337,reuseaddr,fork exec:"./server.py"
