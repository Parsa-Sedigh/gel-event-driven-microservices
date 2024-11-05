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
## 35-004 Adding initialization check