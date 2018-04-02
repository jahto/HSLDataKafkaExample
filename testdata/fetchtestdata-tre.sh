#! /bin/bash
# old http://data.itsfactory.fi/siriaccess/vm/json
# new http://data.itsfactory.fi/journeys/api/1/vehicle-activity
for num in {0..999};
do
curl http://data.itsfactory.fi/journeys/api/1/vehicle-activity > "exampledata-tre-"$num".json";
# Assuming it takes approximately one second to fetch the data, so we get
# something like one sample per 10 seconds.
sleep 9;
done
