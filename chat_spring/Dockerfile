#----------빌드----------------------

FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

COPY --chown=gradle:gradle . .
# 서비스 외부에 있는 proto 가져오기
COPY --from=proto . /proto

RUN gradle clean build -x test --no-daemon
#----------실행----------------------

FROM openjdk:21-ea-21-slim

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT [ "java",  "-jar", "app.jar" ]