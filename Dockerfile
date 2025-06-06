FROM eclipse-temurin:21.0.2_13-jdk-jammy AS builder
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY ./src ./src
RUN ./mvnw dependency:resolve
RUN ./mvnw clean install -Pskip-tests

FROM eclipse-temurin:21.0.2_13-jre-jammy AS final
WORKDIR /usr/handwriting-labeling-app
RUN apt update && apt install -y python3 python3-numpy python3-scipy python3-networkx && ln -s /usr/bin/python3 /usr/bin/python
COPY --from=builder ./target/*.jar *.jar
ENTRYPOINT ["java", "-jar", "./*.jar"]