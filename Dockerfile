FROM alpine:3.18.0
RUN apk add --no-cache openjdk17
WORKDIR /app
COPY build/libs/*.jar app.jar
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseStringDeduplication -Xms512m -Xmx1536m -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom"
CMD ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
