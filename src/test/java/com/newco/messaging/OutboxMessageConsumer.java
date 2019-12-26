package com.newco.messaging;

import com.newco.messaging.model.Message;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class OutboxMessageConsumer implements Runnable {
  private final BlockingQueue<Message> outboxQueue;
  private final Integer sleepTimeBetweenMessages;

  public OutboxMessageConsumer(
      OrderedMessageQueueProvider queueProvider, Integer sleepTimeBetweenMessages) {
    this.outboxQueue = (BlockingQueue<Message>) queueProvider.getQueue();
    this.sleepTimeBetweenMessages = sleepTimeBetweenMessages;
  }

  @Override
  public void run() {
    while (true) {
      try {
        Thread.sleep(sleepTimeBetweenMessages);
        if (!outboxQueue.isEmpty()) {
          Message receivedMessage = outboxQueue.poll(5, TimeUnit.SECONDS);
          System.out.println("Message processed: " + receivedMessage);
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
