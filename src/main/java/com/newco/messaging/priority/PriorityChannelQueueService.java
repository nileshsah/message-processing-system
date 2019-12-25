package com.newco.messaging.priority;

import com.newco.messaging.ChannelQueueService;
import com.newco.messaging.model.Message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class PriorityChannelQueueService implements ChannelQueueService<Message> {

  private final Map<String, Queue<Message>> channelIdQueueMap;
  private final Map<String, Semaphore> channelIdSemaphoreLockMap;
  private final ExecutorService queueMessageListenerPool;
  private final Queue<Message> outQueue;

  public PriorityChannelQueueService(Queue<Message> outQueue, Integer messageThreadPoolSize) {
    this.channelIdQueueMap = Collections.synchronizedMap(new HashMap<String, Queue<Message>>());
    this.channelIdSemaphoreLockMap = Collections.synchronizedMap(new HashMap<String, Semaphore>());
    this.queueMessageListenerPool = Executors.newFixedThreadPool(messageThreadPoolSize);
    this.outQueue = outQueue;
  }

  @Override
  public synchronized void push(String channelId, Message message) {
    channelIdQueueMap.get(channelId).add(message);
    channelIdSemaphoreLockMap.get(channelId).release();
  }

  @Override
  public synchronized void createChannel(String channelId) {
    if (channelIdQueueMap.containsKey(channelId)) {
      throw new IllegalStateException("Channel with ID " + channelId + " already exists");
    }
    channelIdSemaphoreLockMap.put(channelId, new Semaphore(0));
    channelIdQueueMap.put(channelId, new LinkedBlockingQueue<Message>());
  }

  @Override
  public synchronized void deleteChannel(String channelId) {
    if (!channelIdQueueMap.containsKey(channelId)) {
      throw new IllegalStateException("Channel with ID " + channelId + " does not exists");
    }
    channelIdSemaphoreLockMap.remove(channelId);
    channelIdQueueMap.remove(channelId);
  }
}
