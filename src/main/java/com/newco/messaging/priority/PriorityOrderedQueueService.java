package com.newco.messaging.priority;

import com.newco.messaging.OrderedQueueService;
import com.newco.messaging.model.Message;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PriorityOrderedQueueService implements OrderedQueueService<Message> {

  private static final Map<String, Integer> CHANNEL_TRANSFER_SIZE =
      new HashMap<String, Integer>() {
        {
          put("cn-01", 1);
          put("cn-02", 1);
          put("cn-03", 3);
          put("cn-04", 5);
        }
      };

  private final Map<String, Queue<Message>> channelIdQueueMap;
  private final Map<String, QueueMessageConsumer> channelIdConsumerMap;
  private final ExecutorService messageListenerPool;
  private final BlockingQueue<Message> outboxQueue;
  private final Integer internalConsumerPerChannel;
  private final ReadWriteLock operationsLock;

  public PriorityOrderedQueueService(Integer outQueueCapacity, Integer internalConsumerPerChannel) {
    this.channelIdQueueMap = new HashMap<>();
    this.channelIdConsumerMap = new HashMap<>();
    this.messageListenerPool = Executors.newCachedThreadPool();
    this.outboxQueue = new ArrayBlockingQueue<>(outQueueCapacity);
    this.internalConsumerPerChannel = internalConsumerPerChannel;
    this.operationsLock = new ReentrantReadWriteLock();
  }

  @Override
  public void push(String channelId, Message message) {
    Lock readLock = operationsLock.readLock();
    try {
      readLock.lock();
      if (!channelIdQueueMap.containsKey(channelId)) {
        throw new IllegalStateException("The given channel ID " + channelId + " does not exist");
      }
      channelIdQueueMap.get(channelId).add(message);
      channelIdConsumerMap.get(channelId).signalMessageAvailable();
    } finally {
      readLock.unlock();
    }
  }

  @Override
  public void createChannel(String channelId) {
    Lock writeLock = operationsLock.writeLock();
    try {
      writeLock.lock();
      if (channelIdQueueMap.containsKey(channelId)) {
        throw new IllegalStateException("Channel with ID " + channelId + " already exists");
      }
      Queue<Message> channelQueue = new LinkedBlockingQueue<>();
      QueueMessageConsumer consumer =
          new QueueMessageConsumer(channelQueue, outboxQueue, CHANNEL_TRANSFER_SIZE.get(channelId));
      channelIdQueueMap.put(channelId, channelQueue);
      channelIdConsumerMap.put(channelId, consumer);

      for (Integer count = 0; count < internalConsumerPerChannel; count = count + 1) {
        messageListenerPool.submit(consumer);
      }
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public void deleteChannel(String channelId) {
    Lock writeLock = operationsLock.writeLock();
    try {
      writeLock.lock();
      if (!channelIdQueueMap.containsKey(channelId)) {
        throw new IllegalStateException("Channel with ID " + channelId + " does not exists");
      }
      channelIdConsumerMap.get(channelId).stop();

      channelIdQueueMap.remove(channelId);
      channelIdConsumerMap.remove(channelId);
    } finally {
      writeLock.unlock();
    }
  }

  @Override
  public Queue<Message> getQueue() {
    return outboxQueue;
  }
}
