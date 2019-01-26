#!/bin/bash
# trips http://rt.mattersoft.fi/vilkku/api/gtfsrealtime/v1.0/feed/tripupdate
# vehicles http://rt.mattersoft.fi/vilkku/api/gtfsrealtime/v1.0/feed/vehicleposition
for num in {0..999};
do
wget -o /dev/null -O vehicles-"$num".data http://rt.mattersoft.fi/vilkku/api/gtfsrealtime/v1.0/feed/vehicleposition &
wget -o /dev/null  -O trips-"$num".data http://rt.mattersoft.fi/vilkku/api/gtfsrealtime/v1.0/feed/tripupdate &
# Assuming it takes approximately one second to fetch the data, so we get
# something like one sample per 10 seconds.
sleep 10;
done
