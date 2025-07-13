package com.waquarshamsi.api.telegram_notifer.dto;

import com.waquarshamsi.api.telegram_notifer.model.NotificationTarget;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class NotificationRequest {

    @NotBlank(message = "Message body cannot be blank")
    private String body;

    @NotBlank(message = "Recipient identifier cannot be blank")
    private String recipientIdentifier; // e.g., email address or Telegram chat ID

    @NotBlank(message = "Originator cannot be blank")
    private String originator;

    @NotNull(message = "Timestamp cannot be null")
    private Instant timestamp;

    @NotEmpty(message = "Targets list cannot be empty")
    private List<NotificationTarget> targets;

    private Map<String, String> metadata;

    // Getters and Setters

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getRecipientIdentifier() { return recipientIdentifier; }
    public void setRecipientIdentifier(String recipientIdentifier) { this.recipientIdentifier = recipientIdentifier; }

    public String getOriginator() { return originator; }
    public void setOriginator(String originator) { this.originator = originator; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public List<NotificationTarget> getTargets() { return targets; }
    public void setTargets(List<NotificationTarget> targets) { this.targets = targets; }

    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }

    @Override
    public String toString() {
        return "NotificationRequest{" +
                "body='" + body.substring(0, Math.min(body.length(), 30)) + "...'" +
                ", recipientIdentifier='" + recipientIdentifier + '\'' +
                ", originator='" + originator + '\'' +
                ", timestamp=" + timestamp +
                ", targets=" + targets +
                ", metadata=" + metadata +
                '}';
    }
}