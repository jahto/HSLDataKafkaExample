#!/bin/bash
# --compressed http://data.foli.fi/siri/vm
for num in {0..999};
do
curl --compressed http://data.foli.fi/siri/vm  > "exampledata-tku-"$num".json";
# Assuming it takes approximately one second to fetch the data, so we get
# something like one sample per 10 seconds.
sleep 9;
done
