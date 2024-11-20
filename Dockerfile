FROM openjdk:17
COPY ./target/Idiomas-1.jar app.jar
EXPOSE 8134
ENTRYPOINT [ "java", "-jar", "app.jar" ]