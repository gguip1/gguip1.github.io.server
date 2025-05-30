package io.github.gguip1.IntegratedServer.config;

import io.github.gguip1.IntegratedServer.seasons.handler.SeasonWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SeasonWebSocketHandler seasonWebSocketHandler;

    public WebSocketConfig(SeasonWebSocketHandler seasonWebSocketHandler) {
        this.seasonWebSocketHandler = seasonWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(seasonWebSocketHandler, "/seasons")
                .setAllowedOrigins("*");
    }
}
