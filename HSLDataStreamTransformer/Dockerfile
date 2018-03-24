FROM anapsix/alpine-java

RUN mkdir /app
RUN mkdir /app/lib
ADD run-transformer.sh /app
RUN chmod a+x /app/run-transformer.sh
ADD target/jahto-hsl-example-data-stream-transformer-0.1.0-SNAPSHOT.jar /app/lib/app.jar
CMD ["/app/run-transformer.sh"]
