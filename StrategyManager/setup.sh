if [ ! -x "$(command -v docker)" ]; then
     echo 'Docker is not installed. Please install it.'
     exit 1
fi


docker network create nemoulous

docker run --net=nemoulous -d --name=zookeeper -e ZOOKEEPER_CLIENT_PORT=2181 confluentinc/cp-zookeeper:5.3.3

docker run --net=nemoulous -d -p 9092:9092 --name=kafka -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://nemoulous:9092 -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 confluentinc/cp-kafka:5.3.3

docker run --net=nemoulous -d -p 5432:5432 --name=postgres -e POSTGRES_PASSWORD=nemoulous postgres

docker exec postgres /usr/bin/psql -U postgres -c "drop database if exists nemoulous;"

docker exec postgres /usr/bin/psql -U postgres -c "create database nemoulous;"