// Purpose: REST controller for Server-Sent Events (SSE) subscription endpoint with JWT token validation.
// Notes: Enables JavaScript clients to subscribe to real-time notifications over HTTP text/event-stream.

package com.app.cabbie.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("api/user/notifications")
@RequiredArgsConstructor
public class SseController {


    private final SseEmitterRegistry registry;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    // Purpose: Registers a new SSE client connection by extracting and validating the JWT token.
    // Behavior: Validates bearer token, creates a new SseEmitter if valid, returns null if token is malformed or invalid.
    public SseEmitter subscribe(@RequestParam String t){
       return registry.register(t);
    }


}
