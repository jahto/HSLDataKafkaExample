FROM anapsix/alpine-java

RUN mkdir /app
RUN mkdir /app/lib
ADD run-webserver.sh /app
RUN chmod a+x /app/run-webserver.sh
ADD target/TrafficDataWebServer.jar /app/lib/app.jar
CMD ["/app/run-webserver.sh"]
