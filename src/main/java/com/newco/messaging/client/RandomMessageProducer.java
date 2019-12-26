package com.newco.messaging.client;

import com.newco.messaging.model.Message;

public class RandomMessageProducer implements Runnable {
  private final Integer sleepTimeBetweenMessages;

  public RandomMessageProducer(Integer sleepTimeBetweenMessages) {
    this.sleepTimeBetweenMessages = sleepTimeBetweenMessages;
  }

  @Override
  public void run() {
    while (true) {
      try {
        Thread.sleep(sleepTimeBetweenMessages);
        Message message = RandomMessageGenerator.generate();
        ChannelListener.getInstance().enqueueMessage(message);
        System.out.println("Message enqueued: " + message);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
