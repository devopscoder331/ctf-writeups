#!/bin/sh

python3 main.py &
PID=$!

# Auto kill main process in 180 seconds
sleep 180
echo "Ya sam sebya zahuyaril"
kill $PID

exit 1