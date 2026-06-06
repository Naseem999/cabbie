package com.app.cabbie.configuration;

import com.app.cabbie.service.JWTService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@RequiredArgsConstructor
public class JwtChannelInterceptorForWebSocketSecurity implements ChannelInterceptor {

private final JWTService jwtService;
private final UserDetailsService userDetailsService;

    @Override
    // Check STOMP CONNECT frames for `Authorization: Bearer <token>` and validate the JWT.
    // If valid, attach an Authentication principal to the WebSocket session so user destinations work.
    public @Nullable Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor= MessageHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);

        if(accessor !=null && StompCommand.CONNECT.equals(accessor.getCommand())){

            String token=accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                try {

                    final String jwt= token.substring(7);
                    final String userEmail=jwtService.extractUsername(jwt);

                    Authentication authentication= SecurityContextHolder.getContext().getAuthentication();

                    if(userEmail !=null && authentication==null){
                        UserDetails userDetails=userDetailsService.loadUserByUsername(userEmail);

                        if(jwtService.isTokenValid(jwt,userDetails))
                        {
                            UsernamePasswordAuthenticationToken authToken=new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,                          // credentials null after auth
                                    userDetails.getAuthorities()
                            );
                            accessor.setUser(authToken);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }

        }

        return message;
    }
}
