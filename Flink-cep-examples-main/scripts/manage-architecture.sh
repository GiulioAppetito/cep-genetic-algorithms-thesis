#!/bin/bash

# File docker-compose
DOCKER_COMPOSE_FILE="docker-compose.yml"

usage() {
    echo "Usage:"
    echo "  ./manage-architecture.sh --start     # Start architecture"
    echo "  ./manage-architecture.sh --stop      # Stop architecture"
    echo "  ./manage-architecture.sh --restart   # Restart architecture"
}

run_docker_compose() {
    echo "🚀 Starting up architecture ..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" up -d
}

stop_docker_compose() {
    echo "🛑 Stopping architecture ..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" down
}

execute() {
    case "$1" in
        --help) usage ;;
        --start) run_docker_compose ;;
        --stop) stop_docker_compose ;;
        --restart)
            stop_docker_compose
            sleep 5
            run_docker_compose
            ;;
        *) usage ;;
    esac
}

# Execute command
execute "$@"
