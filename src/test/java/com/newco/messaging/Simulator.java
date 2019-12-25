package com.newco.messaging;

import com.newco.messaging.priority.PriorityMessageHandler;
import com.newco.messaging.priority.PriorityOrderedQueueService;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Simulator {

  public static final int OUT_QUEUE_CAPACITY = 100;
  public static final int PRODUCER_COUNT = 10;
  public static final int CONSUMER_COUNT = 6;

  public static void main(String[] args) {
    ExecutorService producerThreadPool = Executors.newCachedThreadPool();
    ExecutorService consumerThreadPool = Executors.newCachedThreadPool();
    Random randomizer = new Random();

    final OrderedQueueService queueService = new PriorityOrderedQueueService(OUT_QUEUE_CAPACITY);
    MessageHandler messageHandler = new PriorityMessageHandler(queueService);

    ChannelListener.getInstance().registerMessageHandler(messageHandler);

    for (int i = 0; i < PRODUCER_COUNT; i++) {
      producerThreadPool.submit(new RandomMessageProducer(randomizer.nextInt(1000)));
    }

    consumerThreadPool.submit(new Runnable() {
      @Override
      public void run() {
        while (true) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          System.out.println("Outbox Queue Size: " + queueService.getQueue().size());
        }
      }
    });

    for (int i = 0; i < CONSUMER_COUNT; i++) {
      consumerThreadPool.submit(new OutboxMessageConsumer(queueService, randomizer.nextInt(500)));
    }
  }
}
