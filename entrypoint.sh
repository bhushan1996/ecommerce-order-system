#!/bin/sh

# Entrypoint script for the E-commerce Order Service
# Handles environment-specific configurations and starts the application

echo "=========================================="
echo "E-commerce Order Processing System"
echo "=========================================="

# Set default values for environment variables if not provided
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-default}
export SERVER_PORT=${SERVER_PORT:-8080}
export REDIS_HOST=${REDIS_HOST:-localhost}
export REDIS_PORT=${REDIS_PORT:-6379}
export DB_URL=${DB_URL:-jdbc:h2:mem:orderdb}
export DB_USERNAME=${DB_USERNAME:-sa}
export DB_PASSWORD=${DB_PASSWORD:-}

echo "Starting application with profile: $SPRING_PROFILES_ACTIVE"
echo "Server port: $SERVER_PORT"
echo "Redis host: $REDIS_HOST:$REDIS_PORT"
echo "Database URL: $DB_URL"
echo "=========================================="

# Start the application with JVM options
exec java \
    -Xms256m \
    -Xmx512m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
    -Dserver.port=$SERVER_PORT \
    -Dspring.data.redis.host=$REDIS_HOST \
    -Dspring.data.redis.port=$REDIS_PORT \
    -Dspring.datasource.url=$DB_URL \
    -Dspring.datasource.username=$DB_USERNAME \
    -Dspring.datasource.password=$DB_PASSWORD \
    -jar /app/app.jar
