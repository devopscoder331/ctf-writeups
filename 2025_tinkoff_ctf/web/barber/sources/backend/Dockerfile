FROM gradle:8-jdk21 as build
WORKDIR /home/gradle/src
copy --chown=gradle:gradle . ./
RUN gradle buildFatJar --no-daemon

FROM openjdk:21
EXPOSE 8080:8080
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar ./ktor-server.jar
COPY flag.txt /home/secret/flag.txt
CMD ["java", "-jar", "/app/ktor-server.jar"]
