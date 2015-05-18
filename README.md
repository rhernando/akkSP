kafka:
========


Descargar
http://kafka.apache.org/downloads.html


1.     Zookeper

definir zookeper.properties
dataDir=/tmp/zookeeper
Levantar zookeper:
zookeeper-server-start.sh config/zookeeper.properties

2.     Kafka broker

server.properties:
* broker.id unico
* log.dir
* zookeeper.connect mismo puerto que zookeeper

Levantar broker:
kafka-server-start.sh config/server.properties

3.     Crear un topic
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic elespanol-topic

Para ver la lista:
bin/kafka-topics.sh --list --zookeeper localhost:2181


4.      Conexion producer

broker.list = localhost:9092
zookeeper: localhost:2181

5.     Consumidor por consola

${KAFKAHOME}/bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic elespanol-topic --from-beginning

6.      Productor por consola

${KAFKAHOME}bin/kafka-console-producer.sh --broker-list localhost:9092 --topic elespanol-topic



Sample logs
-------------

ruben|05/18/2015 15:32:32.607|LOGIN
ruben|05/18/2015 15:32:32.607|ACCESS|354
ruben|05/18/2015 15:32:32.607|SHARE|2568421
ruben|05/18/2015 15:32:32.607|LIKE|254357
ruben|05/18/2015 15:32:32.607|COMMENT|254254|2985
