# Message Processing System

The repository implements an in-memory high-throughput priority-based messaging system in Java 8

## Classes

The **MessageHandler** exposes the required interfaces to interact with the messaging system - for pushing messages and managing channels.
The **OrderedQueueService** serves as the heart of the messaging system and implements the core logic around the creation/deletion of Channels and handling message inflow and outflow.
The **QueueMessageConsumer** is used internally for listening to messages from the internal channel queues, preparing them and pushing them into the outbox queue.

## Flow

### Channel Creation
The **PriorityMessageHandler** delegates the channel creation command to the OrderedQueueService. 
The **PriorityOrderedQueueService** implementation of the OrderedQueueService maintains an internal map of `channelId` to `Queue<Message>`. 
A LinkedBlockingQueue is used for maintaining the messages since it's unbounded and provides thread-safe operations.
For every new channel, a new Queue is created and added to the Map and along with a set of QueryMessageConsumer threads are initialized to operate on this queue.

### Channel Deletion
The **PriorityMessageHandler** delegates the channel deletion command to the OrderedQueueService. 
The **PriorityOrderedQueueService** removes the channelId entry from its internal map and stops all the QueryMessageConsumers working on the queue associated with this channel.

### Channel Message Received
The **PriorityMessageHandler** delegates the message push command to the OrderedQueueService.
The **PriorityOrderedQueueService** inserts the message into the respective queue associated with the channel and signals the QueryMessageConsumer listening on the queue about the availability of the message.

## Design Decisions

* The **PriorityOrderedQueueService** uses `ReadWriteLock` to manage synchronization among push, channel deletion and creation operations.
* **Semaphore** is used by QueryMessageConsumer to signal when the required (transfer size) amount of messages is available so that the thread can be unblocked for message consumption.
* `put` operation is preferred for the outbox queue since it's required for the QueryMessageConsumer to be blocked when the outbox queue is full.

The PriorityOrderedQueueService on receiving a message first inserts the message into the respective queue for the channel and then signals the QueryMessageConsumer (QMC) about the availability of the message which internally just increments the semaphore. Every QMC thread is blocked until it can acquire claims equal to the transfer size for the channel. Once the channel queue has sufficient messages the QMC thread can acquire the semaphore lock and proceeds to take the messages from the channel queue. It then calls the prepare() method on the message before making a blocking call to place this message into the Outbox queue. Since the prepare() method might take a while to execute and blocks the QMC thread meanwhile, we decided to use a group of QMC threads for every internal channel queue.

## Simulator

A simulator program is provided to test the implementation of the messaging system using randomized data. It uses a pool for message producers and message consumers which interacts with the messaging system as a whole and is configurable.

Sample Output
```text
Channel created: cn-01
Message enqueued: Message(id=5115374033600992628, sourceChannelId=cn-01, payload=����X��{ZN���)
Channel created: cn-02
Message enqueued: Message(id=6810218739369952513, sourceChannelId=cn-02, payload=����^^􉔃��̳Y�d�)
Message processed: Message(id=5115374033600992628, sourceChannelId=cn-01, payload=����X��{ZN���)
Message enqueued: Message(id=1049515144721234004, sourceChannelId=cn-01, payload=�)
Message processed: Message(id=6810218739369952513, sourceChannelId=cn-02, payload=����^^􉔃��̳Y�d�)
Channel created: cn-03
Message enqueued: Message(id=657627094127186932, sourceChannelId=cn-03, payload=)
Message enqueued: Message(id=7989637598263088833, sourceChannelId=cn-02, payload=/�r.)
Message enqueued: Message(id=1103883357126701102, sourceChannelId=cn-02, payload=���ȏ�ê�݀}�"��)
Message processed: Message(id=1049515144721234004, sourceChannelId=cn-01, payload=�)
Channel created: cn-04
Message enqueued: Message(id=2148432114271410001, sourceChannelId=cn-04, payload=��{'�P�)
Message processed: Message(id=1103883357126701102, sourceChannelId=cn-02, payload=���ȏ�ê�݀}�"��)
Message enqueued: Message(id=6876384631712837910, sourceChannelId=cn-04, payload=Z#.)
Message enqueued: Message(id=745049232036055115, sourceChannelId=cn-04, payload=Bp8�i�L)
Channel destroyed: cn-04
Message enqueued: Message(id=1732526319845368791, sourceChannelId=cn-04, payload=�|�It�L?~)
Message enqueued: Message(id=5917777655313112374, sourceChannelId=cn-03, payload=5T����7*�)
Message enqueued: Message(id=1937614905576936265, sourceChannelId=cn-01, payload=�z����)
Message processed: Message(id=2148432114271410001, sourceChannelId=cn-04, payload=��{'�P�)
```

