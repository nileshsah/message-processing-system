package com.newco.messaging;

import com.newco.messaging.model.Message;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChannelListener {
  private static ChannelListener instance = new ChannelListener();
  private MessageHandler handler;
  private Set<String> createdChannels;
  private Lock operationsLock;
  private Random randomizer;

  private ChannelListener() {
    this.createdChannels = new HashSet<>();
    this.operationsLock = new ReentrantLock();
    this.randomizer = new Random();
  }

  public static ChannelListener getInstance() {
    return instance;
  }

  public void registerMessageHandler(MessageHandler handler) {
    this.handler = handler;
  }

  public void enqueueMessage(Message message) {
    try {
      operationsLock.lock();
      if (!createdChannels.contains(message.getSourceChannelId())) {
        createChannel(message.getSourceChannelId());
      }
      handler.messageReceived(message);
    } finally {
      operationsLock.unlock();
    }

    boolean shouldDeleteChannel = (randomizer.nextInt(1000) % 5 == 0);
    if (shouldDeleteChannel) {
      removeChannel(message.getSourceChannelId());
    }
  }

  private void removeChannel(String channelId) {
    try {
      operationsLock.lock();
      if (createdChannels.contains(channelId)) {
        handler.channelDestroyed(channelId);
        createdChannels.remove(channelId);
        System.out.println("Channel destroyed: " + channelId);
      }
    } finally {
      operationsLock.unlock();
    }
  }

  private void createChannel(String channelId) {
    handler.channelCreated(channelId);
    createdChannels.add(channelId);
    System.out.println("Channel created: " + channelId);
  }
}
