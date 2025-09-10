package com.techelevator.model.parking;

import java.time.LocalDateTime;

public class SmsMessage {

    private int messageId;
    private Integer sessionId;
    private Integer userId;
    private String phoneNumber;
    private String messageType;
    private String messageContent;
    private String direction;
    private LocalDateTime sentAt;
    private String status;
    private String twilioSid;


    public SmsMessage() { }


    public SmsMessage(int messageId,
                      Integer sessionId,
                      Integer userId,
                      String phoneNumber,
                      String messageType,
                      String messageContent,
                      String direction,
                      LocalDateTime sentAt,
                      String status,
                      String twilioSid) {

        this.messageId = messageId;
        this.sessionId = sessionId;
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.messageType = messageType;
        this.messageContent = messageContent;
        this.direction = direction;
        this.sentAt = sentAt;
        this.status = status;
        this.twilioSid = twilioSid;

    }

    // Getters and Setters

    public int getMessageId() {
        return messageId;
    }
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public Integer getSessionId() {
        return sessionId;
    }
    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getUserId() {
        return userId;
    }
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMessageType() {
        return messageType;
    }
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageContent() {
        return messageContent;
    }
    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getDirection() {
        return direction;
    }
    public void setDirection(String direction) {
        this.direction = direction;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getTwilioSid() {
        return twilioSid;
    }
    public void setTwilioSid(String twilioSid) {
        this.twilioSid = twilioSid;
    }
}
