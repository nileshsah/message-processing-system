package com.newco.messaging;

import com.newco.messaging.model.Message;

public interface MessageHandler {
  void messageReceived(Message message);

  void channelCreated(String channelId);

  void channelDestroyed(String channelId);
}
