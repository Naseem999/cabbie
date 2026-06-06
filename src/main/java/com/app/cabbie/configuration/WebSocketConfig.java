package com.app.cabbie.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptorForWebSocketSecurity jwtChannelInterceptor;

    // Intercepts inbound STOMP messages so WebSocket connections can be authenticated via JWT.
    // Registers the JwtChannelInterceptorForWebSocketSecurity to handle CONNECT frames.
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }

    // Registers the STOMP endpoint used by clients. SockJS enabled for browser fallback.
    // Exposes `/ws` and allows flexible CORS for local development environments.
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    // Configures the in-memory message broker prefixes used by the application.
    // Enables `/queue` simple broker, application prefix `/app`, and user prefix `/user`.
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");

    }
}
