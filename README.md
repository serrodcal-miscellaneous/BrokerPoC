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

## Producer

Under `producer/` directory, there is a Maven project which uses Akka, Akka-Http, Akka-Stram, 
Alpakka in Scala. This is a simple HTTP server which recives request by POST in `/publish` and 
send the message in body to Kafka (topic name is "myTopic").

Build this project as given below:

```bash
~$ mvn clean install
```

Run this server as given below:

```bash
~$ java -jar ./producer/target/kafkaProducer-0.1.0-SNAPSHOT-jar-with-dependencies.jar
```

Or, can provide `-Dconfig.file=./producer/src/main/resources/application.conf` before `-jar` flag.

Open in other terminal window kafkacat as given below:
```bash
~$ kafkacat -C -b localhost:9092 -t myTopic
   % Reached end of topic myTopic [0] at offset 0
   
```

Send your request, for example:

```bash
~$ curl -i -X POST -d 'foo' 'http://localhost:8080/publish' 
   HTTP/1.1 202 Accepted
   Server: akka-http/10.1.5
   Date: Wed, 03 Oct 2018 12:43:20 GMT
   Content-Type: text/plain; charset=UTF-8
   Content-Length: 34
   
   Message recived and sent to Kafka!%  
```

Finally, can see the output in kafkacat terminal window:

```bash
~$ kafkacat -C -b localhost:9092 -t myTopic
   % Reached end of topic myTopic [0] at offset 0
   foo
   % Reached end of topic myTopic [0] at offset 3
```



