package com.newco.messaging.model;

public class Message {
  private Long messageId;
  private String sourceChannelId;
  private Byte[] payload;

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

  public Byte[] getPayload() {
    return payload;
  }

  public void setPayload(Byte[] payload) {
    this.payload = payload;
  }

  public void prepare() {
    try {
      Thread.sleep(Math.round(Math.random()) % 500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}