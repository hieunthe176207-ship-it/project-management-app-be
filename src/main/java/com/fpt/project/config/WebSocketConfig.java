package com.fpt.project.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * - /ws          : endpoint THUẦN WebSocket cho Android (không SockJS)
 * - /ws-sockjs   : endpoint SockJS cho browser (React/Next/Vite...)
 * - Broker: /topic, /queue
 * - App prefix: /app (client SEND tới /app/...)
 * - User prefix: /user (server gửi riêng user -> /user/queue/...)
 *
 * Lưu ý: JWT được kiểm tra ở STOMP CONNECT bởi WebSocketAuthInterceptor
 *        (KHÔNG chặn ở HTTP handshake).
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor; // interceptor tự viết ở dưới

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Android / Mobile: thuần WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // DEV: mở rộng; PROD: đặt đúng origin

        // Browser: SockJS (hỗ trợ fallback + CORS dev)
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // ✅ Kiểm tra JWT ở STOMP CONNECT (đọc header Authorization từ CONNECT)
        registration.interceptors(webSocketAuthInterceptor);
    }
}
