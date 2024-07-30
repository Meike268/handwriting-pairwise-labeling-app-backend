FROM eclipse-temurin:21.0.2_13-jdk-jammy AS builder
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY ./src ./src
RUN ./mvnw clean install -Pskip-tests -Dspring.profiles.active=prod

FROM eclipse-temurin:21.0.2_13-jre-jammy AS final
WORKDIR /usr/handwriting-labeling-app
COPY --from=builder ./target/*.jar *.jar
ENTRYPOINT ["java", "-jar", "./*.jar"]