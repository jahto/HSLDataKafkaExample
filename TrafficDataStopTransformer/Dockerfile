FROM anapsix/alpine-java

RUN mkdir /app
RUN mkdir /app/lib
ADD run-app.sh /app
RUN chmod a+x /app/run-app.sh
ADD target/StopStreamTransformer.jar /app/lib/app.jar
CMD ["/app/run-app.sh"]
