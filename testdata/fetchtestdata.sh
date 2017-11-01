#! /bin/bash
for num in {0..9};
do
curl http://api.digitransit.fi/realtime/vehicle-positions/v1/siriaccess/vm/json > "exampledata"$num".json";
# Assuming it takes approximately one second to fetch the data, so we get
# something like one sample per minute.
sleep 59;
done
