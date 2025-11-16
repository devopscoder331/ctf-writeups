#!/bin/bash
echo $FLAG > /flag_$(head -c 32 /dev/urandom | xxd -p)

unset FLAG

flask run  --host=0.0.0.0 --port=5000