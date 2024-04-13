package com.project1.webapp.chatroom.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final SimpMessageSendingOperations messagingTemplate;
    private final Set<String> onlineUsers;
    private final AtomicInteger onlineUsersCount;

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate, Set<String> onlineUsers) {
        this.messagingTemplate = messagingTemplate;
        this.onlineUsers = onlineUsers;
        this.onlineUsersCount = new AtomicInteger(0);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username != null) {
            logger.info("User disconnected: {}", username);
            onlineUsers.remove(username);
            updateOnlineUserCountAndNotify();
        }
    }

    private void updateOnlineUserCountAndNotify() {
        int count = onlineUsers.size();
        onlineUsersCount.set(count);
        messagingTemplate.convertAndSend("/topic/onlineUsersCount", count);
    }
}
