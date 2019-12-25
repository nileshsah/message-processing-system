package com.newco.messaging.priority;

import com.newco.messaging.ChannelQueueService;
import com.newco.messaging.MessageHandler;
import com.newco.messaging.model.Message;

public class PriorityMessageHandler implements MessageHandler {

  private final ChannelQueueService<Message> channelQueueService;

  public PriorityMessageHandler(ChannelQueueService channelQueueService) {
    this.channelQueueService = channelQueueService;
  }

  @Override
  public void messageReceived(Message message) {
    channelQueueService.push(message.getSourceChannelId(), message);
  }

  @Override
  public void channelCreated(String channelId) {
    channelQueueService.createChannel(channelId);
  }

  @Override
  public void channelDestroyed(String channelId) {
    channelQueueService.deleteChannel(channelId);
  }
}
