# Description

This is just a small test project to bring my occasionally outdated knowledge
about whats going up in Java world somewhat more up-to-date.
Especially with Kafka, Spring and dockerized microservices.

Currently in a very unfinished state 

## Prerequisites

You must have Kafka and Zookeeper running somewhere. I recommend using
https://hub.docker.com/r/wurstmeister/kafka/.

## Running

## Subprojects and services

### MavenKafkaSpringConnector

Very simple. Just polls the endpoint and pushes received list of JSON-data
from HSL real-time feed to a Kafka stream as separate messages. More connectors
should be added later.

### MavenKafkaSpringStreamTransformer

This one uses Kafka Streams / Spring integration.

### MavenKafkaSpringListeners


### MavenKafkaSpringListeners.NET

Of course there needs also to be a dockerized .NET Core 2 version serving the same data to clients.

Unfortunately, it hasn't been written (yet)....

### MavenKafkaSpringDataContracts

The name in itself should be quite self-explaining.

Note anyway that I will probably later test Avro
as the internal serialization format instead of JSON. Just for the fun of it,
and also to learn something new.

