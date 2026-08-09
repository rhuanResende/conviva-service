FROM maven:3.9-eclipse-temurin-21 AS build

ARG GITHUB_USERNAME
ARG GITHUB_TOKEN

WORKDIR /app

RUN mkdir -p /root/.m2 && \
    cat > /root/.m2/settings.xml <<EOF
<settings>
    <servers>
        <server>
            <id>github-conviva-security</id>
            <username>${GITHUB_USERNAME}</username>
            <password>${GITHUB_TOKEN}</password>
        </server>
    </servers>
</settings>
EOF

COPY pom.xml .
COPY src ./src

RUN mvn clean package -U -DskipTests && \
    rm -f /root/.m2/settings.xml

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]