FROM openjdk:8-alpine

COPY target/uberjar/sample-1.jar /sample-1/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/sample-1/app.jar"]
