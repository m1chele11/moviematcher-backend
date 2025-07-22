

FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/moviematcher-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8080

EXPOSE $PORT

CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
#ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=$PORT"]

