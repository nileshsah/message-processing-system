package com.newco.messaging;

import com.newco.messaging.model.Message;

import java.util.Queue;

public interface OrderedMessageQueueProvider {
  Queue<Message> getQueue();
}
