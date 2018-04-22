FROM anapsix/alpine-java

RUN mkdir /app
RUN mkdir /app/lib
ADD run-connector.sh /app
RUN chmod a+x /app/run-connector.sh
ADD target/FOLIDataConnector.jar /app/lib/app.jar
CMD ["/app/run-connector.sh"]
