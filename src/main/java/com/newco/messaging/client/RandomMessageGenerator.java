package com.newco.messaging.client;

import com.newco.messaging.model.Message;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RandomMessageGenerator {

  private static final List<String> CHANNEL_ID_LIST = Arrays.asList("cn-01", "cn-02", "cn-03", "cn-04");

  public static Message generate() {
    Random randomizer = new Random();
    String channelId = CHANNEL_ID_LIST.get(randomizer.nextInt(CHANNEL_ID_LIST.size()));
    Long messageId = Math.abs(randomizer.nextLong());
    byte[] payload = new byte[randomizer.nextInt(20)];
    randomizer.nextBytes(payload);

    Message randomMessage = new Message();
    randomMessage.setMessageId(messageId);
    randomMessage.setSourceChannelId(channelId);
    randomMessage.setPayload(payload);

    return randomMessage;
  }

}
