package com.newco.messaging.priority;

import com.newco.messaging.OrderedQueueService;
import com.newco.messaging.MessageHandler;
import com.newco.messaging.model.Message;

public class PriorityMessageHandler implements MessageHandler {

  private final OrderedQueueService<Message> orderedQueueService;

  public PriorityMessageHandler(OrderedQueueService orderedQueueService) {
    this.orderedQueueService = orderedQueueService;
  }

  @Override
  public void messageReceived(Message message) {
    orderedQueueService.push(message.getSourceChannelId(), message);
  }

  @Override
  public void channelCreated(String channelId) {
    orderedQueueService.createChannel(channelId);
  }

  @Override
  public void channelDestroyed(String channelId) {
    orderedQueueService.deleteChannel(channelId);
  }
}
