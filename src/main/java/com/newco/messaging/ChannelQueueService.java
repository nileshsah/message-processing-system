package com.newco.messaging;

import com.newco.messaging.model.Message;

public interface ChannelQueueService<T> {
  void push(String channelId, T message);
  void createChannel(String channelId);
  void deleteChannel(String channelId);
}
