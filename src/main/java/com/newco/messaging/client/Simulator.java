package com.newco.messaging.client;

import com.newco.messaging.MessageHandler;
import com.newco.messaging.OrderedQueueService;
import com.newco.messaging.priority.PriorityMessageHandler;
import com.newco.messaging.priority.PriorityOrderedQueueService;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Simulator {

  public static final int OUT_QUEUE_CAPACITY = 100;
  public static final int INTERNAL_CONSUMER_PER_CHANNEL = 3;
  public static final int PRODUCER_COUNT = 10;
  public static final int CONSUMER_COUNT = 10;

  public static void main(String[] args) {
    ExecutorService producerThreadPool = Executors.newCachedThreadPool();
    ExecutorService consumerThreadPool = Executors.newCachedThreadPool();
    Random randomizer = new Random();

    final OrderedQueueService queueService =
        new PriorityOrderedQueueService(OUT_QUEUE_CAPACITY, INTERNAL_CONSUMER_PER_CHANNEL);
    final MessageHandler messageHandler = new PriorityMessageHandler(queueService);

    ChannelListener.getInstance().registerMessageHandler(messageHandler);

    for (int count = 0; count < CONSUMER_COUNT; count = count + 1) {
      consumerThreadPool.submit(new OutboxMessageConsumer(queueService, randomizer.nextInt(300)));
    }

    for (int count = 0; count < PRODUCER_COUNT; count = count + 1) {
      producerThreadPool.submit(new RandomMessageProducer(randomizer.nextInt(600)));
    }

    consumerThreadPool.submit(
        new Runnable() {
          @Override
          public void run() {
            while (true) {
              try {
                Thread.sleep(1000);
                System.out.println("Outbox Queue Size: " + queueService.getQueue().size());
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
            }
          }
        });

  }
}
