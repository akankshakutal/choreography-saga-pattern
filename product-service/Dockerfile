# BUILDER
FROM bilalfazlani/sbt:latest AS builder

ADD . /app
WORKDIR /app
RUN ./build.sh


# RUNNER
FROM openjdk:11 as runner

COPY --from=builder /app/product-service.bin /
ENTRYPOINT /product-service.bin