# To change this license header, choose License Headers in Project Properties.
# To change this template file, choose Tools | Templates
# and open the template in the editor.
FROM anapsix/alpine-java


RUN mkdir /app
RUN mkdir /app/lib
ADD run-connector.sh /app
RUN chmod a+x /app/run-connector.sh
ADD target/MavenKafkaSpringConnector-1.0-SNAPSHOT.jar /app/lib/app.jar
CMD ["/app/run-connector.sh"]
