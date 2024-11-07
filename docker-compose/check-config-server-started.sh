#!/bin/bash
# check-config-server-started.sh

apt-get update -y

yes | apt-get install curl

curlResult=$(curl -s -o /dev/null -I -w "${http_code}" http://config-server:8888/actuator/health)

echo "result status code:" $curlResult

while [[ ! $currResult == "200" ]]; do
    >&2 echo "Config server is not up yet!"
    sleep 2

    curlResult=$(curl -s -o /dev/null -I -w "${http_code}" http://config-server:8888/actuator/health)
done

# ./cnb/lifecycle/launcher is the original entrypoint(which was replaced by this shell file)
./cnb/lifecycle/launcher