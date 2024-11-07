# Section 05 - kafka-to-elastic-service  How to use Kafka consumers and Elastic Index API
## 32-001 Introduction to Kafka Consumer Reading data events from Kafka using messaging
Note:
- Each partition is assigned to one consumer in a consumer group. This is to prevent conflicts with the offset values in a consumer group.
- kafka allows to read the same data more than once but for that, you need to create a new consumer group and read the data again. 
- for max concurrency, set number of consumers should be equal to number of partitions. If #consumers > #partitions, some consumers will be idle, as
it is not possible to assign a single partition to more than one consumer in the same consumer group.

Note: In slide, the producer produces data according to a partition strategy which can be hashing with key where `hash(key) % #paritions` will
give the target partition number. Or it can be based on round-robin.

If you have a single consumer with multiple partitions, there will be no concurrent work on multiple partitions by default. But we can use
multiple threads while consuming partitions.

Note: We can scale consuming by adding more partitions. So adding a new partition, gives us a chance to create a new consumer.

At least once: Occurs when consumer commits the offset **after processing** the data. The commit op might fail, but consumer has already processed the data.
So in the next poll, it will read the same data and this implies at least once behavior.

Committing before processing means at most once because if consumer encounters err when processing the data, we might lose some data which
implies at most once semantic(the msg is already committed).

autocommit is set to true on consumer. So it commits after a configured timer but in some scenarios you might still encounter an err
while using autocommit because a failure can always happen. For this, you need to implement some rollback mechanism or use exactly once
semantic depending on your needs. But if at least once is good enough, you don't need to think about exactly once.

## 33-002 Adding kafka-consumer module
CPU stall: bad cpu utilization - cpu doesn't make progress although it is running.

## 34-003 Creating the microservice kafka-to-elastic-service
To be able to use our config-server, the microservice needs two `spring-cloud-starter-config` and `spring-cloud-starter-security` deps.

@KafkaListener: creates a kafka consumer.

Note: When sth in config-server-repository is changed, before running microservices to get the new changes you should push those changes to the
related remote git repo.

For now, set auto-startup to true, since we're sure that kafka cluster and other services are already running. So no need to
check initialization logic. Which we will add later and set auto-startup to false.

```shell
mvn install -DskipTests

# in docker-compose folder
docker compose up
docker logs -f <contianer id of twitter-to-kafka-service>
```

## 35-004 Adding initialization check
@EventListener: marks a method as a listener for application events.

## 36-005 Introducing Elasticsearch
- Elastic search runs a apache lucene engine in the background.
- Elastic search organizes data using documents and makes them easily accessible and searchable.
- Elastic search has built-in type guessing and dynamic mapping which will understand the type of a field even though it's not explicitly
defined.

**Note: You can only find the terms that exist in your index.**

Q: How does elasticsearch processes a text before indexing(normalizing)?

A: It uses built in analyzers. Those 3 steps in the slide.

## 37-006 Running elastic search with docker
We create 3 nodes for elastic search like kafka cluster which has 3 nodes. This number is chosen to accomplish quorum and to prevent
split-brain issue while maximizing number of nodes.

- Quorum: set the minimum number of nodes to create a network - prevent split brain.
- prevent split brain: maximize number of nodes which could be down at a moment

It's recommended to use **odd number of master eligible** nodes. Note that with 4 nodes, we can **still** have at most one node to be down
to still have a cluster. So having 4 nodes is no different from having 3. We can still tolerate 1 node being down.
We can only increase this number with odd number of nodes, such as 3 or 5 or ... . With 5 nodes, we can tolerate two nodes being down
and still have a cluster.

Note: if you use only two nodes for example, maximum number of master nodes must be 2. In that case, if even one node is down,
you can't maintain a cluster. That's why to tolerate one node being down, we need at least 3 nodes to maintain a cluster to accomplish 
a quorum.

**elasticsearch quorum formula: (master_eligible_nodes / 2) + 1**

this property is necessary to prevent swapping. Most operating systems try to use as much memory as possible for
the filesystem caches and eagerly swap out unused application memory. This can result in parts of the jvm heap or even
it's executable pages being swapped out to disk. Swapping is very bad for performance and for node stability and
should be avoided at all costs. It can use garbage collections to last for minutes instead of milliseconds and can
cause nodes to response slowly or even to disconnect from the cluster.
In a resilient distributed system it is more effective to let the OS kill the node. bootstrap.memory_lock=true

  
```shell
docker compose -f common.yml -f elastic_cluster.yml up
```
Then send a GET to localhost:9200 which is elastic-1 node.

Now we can use the mapping definition in the slides(slide with index API header) to create an index called `twitter-index`.
So put the json in body of PUT req and send it to `localhost:9200/twitter-index`.

After creating the index, create a document with `POST twitter-index/_doc/1`.

## 38-007 Creating elastic-model module
@Document: indicates this class is a candidate for mapping to elasticsearch.

## 39-008 Creating elastic-config module
## 40-009 Creating elastic-index-client module
## 41-010 Using Elasticsearch repositories for indexing
## 42-011 Integrating elastic modules with microservice
## 43-012 Containerization of microservice with docker image Run all with docker compose