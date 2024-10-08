FROM ghcr.io/navikt/baseimages/temurin:21

COPY build/libs/pensjon-refusjonskrav.jar /app/app.jar
