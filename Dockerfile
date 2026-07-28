FROM eclipse-temurin:26-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -DskipTests

COPY src/ src/
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:26-jre-ubi10-minimal

WORKDIR /app

COPY --from=build /app/target/warehouse-fullfilment-system-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]