package io.github.gguip1.IntegratedServer.seasons.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gguip1.IntegratedServer.seasons.message.SeasonChangeDto;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SeasonWebSocketHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private String currentSeason = "spring";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        SeasonChangeDto dto = new SeasonChangeDto(
                "seasonUpdate",
                currentSeason,
                Instant.now().toString()
        );

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonMessage = objectMapper.writeValueAsString(dto);

        session.sendMessage(new TextMessage(jsonMessage));
        System.out.println("새로운 연결 : " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("받은 메시지 : " + payload);

        ObjectMapper objectMapper = new ObjectMapper();
        SeasonChangeDto incomingDto = objectMapper.readValue(payload, SeasonChangeDto.class);

        if (!"changeRequest".equals(incomingDto.getType())) {
            System.out.println("잘못된 메시지 타입: " + incomingDto.getType());
            return;
        }

        currentSeason = incomingDto.getSeason();

        SeasonChangeDto dto = new SeasonChangeDto(
                "seasonUpdate",
                currentSeason,
                Instant.now().toString()
        );

        String jsonMessage = objectMapper.writeValueAsString(dto);

        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(jsonMessage));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("연결 종료 : " + session.getId());
    }
}
