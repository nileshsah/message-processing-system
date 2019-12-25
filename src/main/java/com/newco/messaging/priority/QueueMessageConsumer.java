package com.newco.messaging.priority;

import com.newco.messaging.model.Message;

import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class QueueMessageConsumer implements Runnable {

  private final Semaphore sharedLock;
  private final Queue<Message> messageQueue;
  private final Queue<Message> outboxQueue;
  private final Integer batchConsumptionSize;
  private final AtomicBoolean isActive;

  public QueueMessageConsumer(Queue<Message> messageQueue, Queue<Message> outboxQueue, Integer batchSize) {
    this.sharedLock = new Semaphore(0);
    this.isActive = new AtomicBoolean(true);
    this.messageQueue = messageQueue;
    this.outboxQueue = outboxQueue;
    this.batchConsumptionSize = batchSize;
  }

  @Override
  public void run() {
    while (isActive.get()) {
      try {
        sharedLock.acquire(batchConsumptionSize);
      } catch (InterruptedException e) {
        throw new RuntimeException("The Queue consumer thread was interrupted", e);
      }

      if (isActive.get()) {
        doConsume(batchConsumptionSize);
      }
    }
  }

  public void signalMessageAvailable() {
    sharedLock.release();
  }

  public void stop() {
    isActive.set(false);
    sharedLock.release(batchConsumptionSize);
  }

  private void doConsume(Integer messagesToConsume) {
    while (messagesToConsume > 0) {
      Message availableMessage = messageQueue.poll();
      availableMessage.prepare();
      outboxQueue.add(availableMessage);
      messagesToConsume = messagesToConsume - 1;
    }
  }
}