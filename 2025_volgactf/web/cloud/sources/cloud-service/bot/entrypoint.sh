while true; do
    socat \
        TCP-LISTEN:5444,reuseaddr,fork \
        SYSTEM:"timeout -s SIGKILL 600 node ./bot.js"
done &

sleep infinity