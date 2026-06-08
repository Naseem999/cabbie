// Purpose: Central registry managing SSE connections for each user with automatic cleanup on disconnect.
// Notes: Thread-safe using ConcurrentHashMap and CopyOnWriteArrayList; handles connection lifecycle via callbacks.

package com.app.cabbie.sse;

import com.app.cabbie.dto.KafkaEventDTO;
import com.app.cabbie.service.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
public class SseEmitterRegistry {

    private final Map<String, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    private final JWTService jwtService;

    private final UserDetailsService userDetailsService;

    public SseEmitter register(String token){
        // Purpose: Validates JWT bearer token and creates a new SSE emitter if authentication succeeds.
        // Behavior: Returns a non-timeout SseEmitter stored in registry; registers cleanup callbacks for graceful disconnection.
        if (token != null && token.startsWith("Bearer ")) {
            try {

                final String jwt= token.substring(7);
                final String userEmail=jwtService.extractUsername(jwt);

                if(userEmail !=null){
                    UserDetails userDetails=userDetailsService.loadUserByUsername(userEmail);

                    if(jwtService.isTokenValid(jwt,userDetails))
                    {
                        // Long.MAX_VALUE means the connection never times out on our end
                        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

                        // Store this emitter under the userId
                        // computeIfAbsent means: if userId not in map, create a new empty list first
                        userEmitters.computeIfAbsent(userEmail, k-> new CopyOnWriteArrayList<>())
                                .add(emitter);

                        // Cleanup: when connection closes/errors, remove from map
                        emitter.onCompletion(()-> remove(userEmail,emitter));
                        emitter.onTimeout(() -> remove(userEmail,emitter));
                        emitter.onError(e  -> remove(userEmail, emitter));

                        System.out.println("User connected: " + userEmail +
                                " | Total connections: " + userEmitters.size());
                        return emitter;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
        return null;
    }

    // Purpose: Sends a Kafka event to all active SSE connections for a specific user.
    // Behavior: Retrieves emitters for userEmail, sends event; removes emitter on IOException to prevent stale connections.
    public void sendToUser(String eventName,KafkaEventDTO dto){

        List<SseEmitter> emitters=userEmitters.getOrDefault(dto.getUserEmail(),List.of());

        for(SseEmitter emitter: emitters){
            try {
                emitter.send(
                        SseEmitter.event()
                                .name(eventName)
                                .data(dto)
                );
            }catch (IOException e   ){
                remove(dto.getUserEmail(), emitter);
            }

        }
    }

    // Purpose: Removes a specific SSE emitter from the user's connection list during disconnect/error.
    // Behavior: Cleans up the userEmail entry entirely if no emitters remain for that user (prevents memory leaks).
    private void remove(String userEmail, SseEmitter sseEmitter){
        List<SseEmitter> emitters=userEmitters.get(userEmail);
        if(emitters!=null){
            emitters.remove(sseEmitter);
            if(emitters.isEmpty()){
                userEmitters.remove(userEmail);
            }
        }
    }
}
