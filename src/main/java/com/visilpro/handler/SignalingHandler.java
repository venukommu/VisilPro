package com.visilpro.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SignalingHandler extends TextWebSocketHandler {

    // Map sessionId -> Set of WebSocketSessions
    private final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Connection established, waiting for 'join' message to assign to a room
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode jsonMessage = objectMapper.readTree(message.getPayload());
        String type = jsonMessage.get("type").asText();

        if ("join".equals(type)) {
            String sessionId = jsonMessage.get("sessionId").asText();
            sessions.computeIfAbsent(sessionId, k -> Collections.synchronizedSet(new HashSet<>())).add(session);
            session.getAttributes().put("sessionId", sessionId);
            System.out.println("Session " + session.getId() + " joined room " + sessionId);
        } else {
            // Relay message to other peers in the same room
            String sessionId = (String) session.getAttributes().get("sessionId");
            if (sessionId != null && sessions.containsKey(sessionId)) {
                for (WebSocketSession peer : sessions.get(sessionId)) {
                    if (!peer.getId().equals(session.getId()) && peer.isOpen()) {
                        peer.sendMessage(message);
                    }
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = (String) session.getAttributes().get("sessionId");
        if (sessionId != null && sessions.containsKey(sessionId)) {
            sessions.get(sessionId).remove(session);
            if (sessions.get(sessionId).isEmpty()) {
                sessions.remove(sessionId);
            }
        }
        System.out.println("Session " + session.getId() + " disconnected");
    }
}
