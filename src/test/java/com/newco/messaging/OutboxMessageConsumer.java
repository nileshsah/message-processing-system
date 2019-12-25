package com.newco.messaging;

import com.newco.messaging.model.Message;

import java.util.Queue;

public class OutboxMessageConsumer implements Runnable {
  private final Queue<Message> outboxQueue;
  private final Integer sleepTimeBetweenMessages;

  public OutboxMessageConsumer(
      OrderedMessageQueueProvider queueProvider, Integer sleepTimeBetweenMessages) {
    this.outboxQueue = queueProvider.getQueue();
    this.sleepTimeBetweenMessages = sleepTimeBetweenMessages;
  }

  @Override
  public void run() {
    while (true) {
      try {
        Thread.sleep(sleepTimeBetweenMessages);
        if (!outboxQueue.isEmpty()) {
          Message receivedMessage = outboxQueue.poll();
          System.out.println("Message processed: " + receivedMessage);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
