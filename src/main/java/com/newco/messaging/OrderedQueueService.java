package com.newco.messaging;

public interface OrderedQueueService<T> {
  void push(String channelId, T message);

  void createChannel(String channelId);

  void deleteChannel(String channelId);
}
