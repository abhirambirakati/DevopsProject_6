FROM eclipse-temurin:21
WORKDIR /app
COPY target/voltstore-1.0.0.jar app.jar
EXPOSE 9091
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=9091"]