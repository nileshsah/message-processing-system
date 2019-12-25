package com.newco.messaging.model;

import java.nio.charset.Charset;
import java.util.Random;

public class Message {
  private Long messageId;
  private String sourceChannelId;
  private byte[] payload;

  public Long getMessageId() {
    return messageId;
  }

  public void setMessageId(Long messageId) {
    this.messageId = messageId;
  }

  public String getSourceChannelId() {
    return sourceChannelId;
  }

  public void setSourceChannelId(String sourceChannelId) {
    this.sourceChannelId = sourceChannelId;
  }

  public byte[] getPayload() {
    return payload;
  }

  public void setPayload(byte[] payload) {
    this.payload = payload;
  }

  public void prepare() {
    try {
      Thread.sleep(new Random().nextInt(600));
    } catch (InterruptedException e) {
      throw new RuntimeException("Thread interrupted while preparing message", e);
    }
  }

  @Override
  public String toString() {
    return "Message(id="
        + this.getMessageId()
        + ", sourceChannelId="
        + this.getSourceChannelId()
        + ", payload="
        + new String(this.getPayload(), Charset.forName("UTF-8"))
        + ") ";
  }
}
