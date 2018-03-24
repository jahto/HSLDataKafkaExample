# Description

This is just a small test project to bring my occasionally outdated knowledge
about whats going up in Java world somewhat more up-to-date.
Especially with Kafka, Spring and dockerized microservices. Uses
HSL realtime data feed of vehicle positions as data source.

Currently in a very unfinished state. Only suitable for looking around,
you might get into some troubles when trying to run. Needs badly instructions
on how to set up and run.

## Prerequisites

You must have Kafka and Zookeeper running somewhere. I recommend using
https://hub.docker.com/r/wurstmeister/kafka/.

Also depends on my other small project https://github.com/jahto/KafkaStreamsStateUtils.
Clone it to the parent directory of this project before trying to compile. Should
come available on Maven central before the end of April 2018.

## Running

## Subprojects and services

### HSLDataConnector

Very simple. Just polls the endpoint (http://api.digitransit.fi/realtime/vehicle-positions/v1/siriaccess/vm/json)
and pushes received list of JSON-data from HSL real-time feed to a Kafka stream as separate messages. More connectors
could be added later.

### HSLDataStreamTransformer

- Adds some data to vehicle information. Difference of delay since last received sample and the length of time the difference
was measured in.
- Adds vehicles operating on a line as a list to line information and pushes the data downstream.
- Collects rough history of a vehicles movement around keyed by vehicle id and date,  and pushes the data downstream.

### HSLDataWebServer

Serves JSON-formatted data from the streams constructed in HSLDataStreamTransformer.

### HSLDataWebServer.NET

Of course there needs also to be a dockerized .NET Core 2 version serving the same data to clients.

Unfortunately, it hasn't been written (yet)... Depends on https://github.com/confluentinc/confluent-kafka-dotnet
next version to be released first.

### HSLDataDataContracts

The name in itself should be quite self-explaining.

## To do
- Test Avro as the internal serialization format instead of JSON. Just for the fun of it,
also to learn something new, and it's rumoured be more compact and faster.
- Steal a good UI from some project at https://github.com/HSLdevcom and modify it to present
data received from web server.
- Clean up this mess of code...