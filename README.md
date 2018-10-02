# BrokerPoC

Simple Kafka consumer and Kafka consumer using Akka/Scala.

## Docker compose

Here there is a `docker-compose.yml` file to start up a Kafka server (and Zookeeper, mandatory to be used by Kafka):

```bash
~$ docker-compose up
```

### Kafka simple command client

To test Kafka server, please install `kafkacat`:

```bash
~$ brew install kafkacat
```

or 

```bash
~$ apt-get install kafkacat
```

#### Using kafkacat

* Get server info (brokers and topics): 

```bash
~$ kafkacat -L -b localhost
```

* Produce a message in a given topic:

```bash
~$ echo "foo" | kafkacat -P -b localhost:9092 -t mytopic
```

* Consume a message from a given topic:

```bash
~$ kafkacat -C -b localhost:9092 -t mytopic
```

