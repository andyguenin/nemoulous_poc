if [ ! -x "$(command -v docker)" ]; then
     echo 'Docker is not installed. Please install it.'
     exit 1
fi


docker network create nemoulous

docker run --net=nemoulous -d -p 2181:2181 --name=zookeeper -e ZOOKEEPER_CLIENT_PORT=2181 confluentinc/cp-zookeeper:5.3.3

docker run -d --name kafka --network nemoulous -p 9092:9092 -e ALLOW_PLAINTEXT_LISTENER=yes -e KAFKA_CFG_ZOOKEEPER_CONNECT=zookeeper:2181 bitnami/kafka:latest

docker run --net=nemoulous -d -p 5432:5432 --name=postgres -e POSTGRES_PASSWORD=nemoulous postgres

docker exec postgres /usr/bin/psql -U postgres -c "drop database if exists nemoulous;"

docker exec postgres /usr/bin/psql -U postgres -c "create database nemoulous;"