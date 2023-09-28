FROM gcr.io/distroless/java17
COPY target/search*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar","-Xmx=512M"]