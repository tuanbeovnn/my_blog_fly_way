package com.myblogbackend.blog.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {
    private UUID postId;          // ID of the post that triggered the notification
    private List<UUID> userIds;   // List of user IDs to whom notifications are sent
    private String message;         // Optional message to include in the notification
    private String notificationType; // Type of notification (e.g., "NEW_POST", "COMMENT", etc.)
    private String deviceTokenId;
}