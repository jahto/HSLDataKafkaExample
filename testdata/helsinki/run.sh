#!/bin/bash

mqtt subcribe --hostname mqtt.hsl.fi --protocol mqtts --port 443 --verbose --topic "/hfp/v1/journey/#" > mqtt.data &

prog_pid=$!

sleep 7200

kill $prog_pid
