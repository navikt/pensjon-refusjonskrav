FROM gcr.io/distroless/java21-debian13:nonroot

ENV TZ="Europe/Oslo"

COPY build/libs/pensjon-refusjonskrav.jar /app/app.jar

CMD ["-jar", "/app/app.jar"]
