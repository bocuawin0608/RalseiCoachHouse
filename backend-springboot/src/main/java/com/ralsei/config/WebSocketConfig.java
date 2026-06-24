package com.ralsei.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefix for messages sent from the server to the client
        config.enableSimpleBroker("/topic");
        // Prefix for messages sent from the client to the server (optional here)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint React will connect to
        registry.addEndpoint("/ws-payment")
                .setAllowedOrigins("https://localhost:3000") // Allow your React app
                .withSockJS(); // Fallback for browsers that don't support WebSockets
    }
}