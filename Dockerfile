FROM eclipse-temurin:26-jre-ubi10-minimal

WORKDIR /app

COPY target/warehouse-fullfilment-system-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]