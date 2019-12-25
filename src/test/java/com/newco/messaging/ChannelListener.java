package com.newco.messaging;

import com.newco.messaging.model.Message;

import java.util.HashSet;
import java.util.Set;

public class ChannelListener {
  private static ChannelListener instance;
  private MessageHandler handler;
  private Set<String> createdChannels;

  private ChannelListener() {
    this.createdChannels = new HashSet<>();
  }

  public static synchronized ChannelListener getInstance() {
    if (instance == null) {
      instance = new ChannelListener();
    }
    return instance;
  }

  public void registerMessageHandler(MessageHandler handler) {
    this.handler = handler;
  }

  public void enqueueMessage(Message message) {
    if (!createdChannels.contains(message.getSourceChannelId())) {
      handler.channelCreated(message.getSourceChannelId());
      createdChannels.add(message.getSourceChannelId());
    }
    handler.messageReceived(message);
  }

}
