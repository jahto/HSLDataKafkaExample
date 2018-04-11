FROM anapsix/alpine-java

RUN mkdir /app
RUN mkdir /app/lib
ADD run-webserver.sh /app
RUN chmod a+x /app/run-webserver.sh
ADD target/jahto-hsl-example-data-web-server-0.1.0-SNAPSHOT.jar /app/lib/app.jar
CMD ["/app/run-webserver.sh"]
