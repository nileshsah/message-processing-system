package com.newco.messaging.priority;

import com.newco.messaging.OrderedQueueService;
import com.newco.messaging.OrderedMessageQueueProvider;
import com.newco.messaging.model.Message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class PriorityOrderedQueueService implements OrderedQueueService<Message>, OrderedMessageQueueProvider  {

  private static final Map<String, Integer> CHANNEL_TRANSFER_SIZE = new HashMap<String, Integer>() {
    {
      CHANNEL_TRANSFER_SIZE.put("cn-01", 1);
      CHANNEL_TRANSFER_SIZE.put("cn-02", 1);
      CHANNEL_TRANSFER_SIZE.put("cn-03", 3);
      CHANNEL_TRANSFER_SIZE.put("cn-04", 5);
    }
  };

  private final Map<String, Queue<Message>> channelIdQueueMap;
  private final Map<String, QueueMessageConsumer> channelIdConsumerMap;
  private final ExecutorService messageListenerPool;
  private final Queue<Message> outboxQueue;

  public PriorityOrderedQueueService(Integer outQueueCapacity) {
    this.channelIdQueueMap = Collections.synchronizedMap(new HashMap<String, Queue<Message>>());
    this.channelIdConsumerMap = Collections.synchronizedMap(new HashMap<String, QueueMessageConsumer>());
    this.messageListenerPool = Executors.newCachedThreadPool();
    this.outboxQueue = new ArrayBlockingQueue<>(outQueueCapacity);
  }

  @Override
  public synchronized void push(String channelId, Message message) {
    channelIdQueueMap.get(channelId).add(message);
    channelIdConsumerMap.get(channelId).signalMessageAvailable();
  }

  @Override
  public synchronized void createChannel(String channelId) {
    if (channelIdQueueMap.containsKey(channelId)) {
      throw new IllegalStateException("Channel with ID " + channelId + " already exists");
    }
    Queue<Message> channelQueue = new LinkedBlockingQueue<>();
    QueueMessageConsumer consumer = new QueueMessageConsumer(channelQueue, outboxQueue, CHANNEL_TRANSFER_SIZE.get(channelId));
    channelIdQueueMap.put(channelId, channelQueue);
    channelIdConsumerMap.put(channelId, consumer);

    messageListenerPool.submit(consumer);
  }

  @Override
  public synchronized void deleteChannel(String channelId) {
    if (!channelIdQueueMap.containsKey(channelId)) {
      throw new IllegalStateException("Channel with ID " + channelId + " does not exists");
    }
    channelIdQueueMap.remove(channelId);
    channelIdConsumerMap.get(channelId).stop();
    channelIdConsumerMap.remove(channelId);
  }

  @Override
  public Queue<Message> getQueue() {
    return outboxQueue;
  }
}
