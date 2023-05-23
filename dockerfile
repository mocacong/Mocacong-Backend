FROM adoptopenjdk/openjdk11:jre-11.0.6_10-alpine
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java","-jar", "-Dspring.profiles.active=prod", "/app.jar"]
