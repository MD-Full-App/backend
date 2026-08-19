# 1-ci Mərhələ: Build mərhələsi
FROM gradle:8.4-jdk17 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN gradle build -x test --no-daemon

# 2-ci Mərhələ: Run mərhələsi
FROM alpine:3.18.0
RUN apk add --no-cache openjdk17
WORKDIR /app
# Build mərhələsində yaranan jar faylını bura kopyalayırıq
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
CMD ["java", "-jar", "app.jar"]
