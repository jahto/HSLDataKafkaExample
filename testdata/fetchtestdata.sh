#! /bin/bash
for num in {0..999};
do
curl http://api.digitransit.fi/realtime/vehicle-positions/v1/siriaccess/vm/json > "exampledata-hsl-"$num".json";
# Assuming it takes approximately one second to fetch the data, so we get
# something like one sample per 10 seconds.
sleep 9;
done
