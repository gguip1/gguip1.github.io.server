package io.github.gguip1.IntegratedServer.seasons.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.gguip1.IntegratedServer.seasons.message.ConnectionCountDto;
import io.github.gguip1.IntegratedServer.seasons.message.SeasonDto;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class SeasonWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SeasonWebSocketHandler.class);

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AtomicInteger connectionCount = new AtomicInteger(0);
    private String currentSeason = "spring";

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        sessions.add(session);

        SeasonDto seasonDto = new SeasonDto(
                "seasonUpdate",
                currentSeason,
                Instant.now().toString()
        );

        ConnectionCountDto connectionCountDto = new ConnectionCountDto(
                "connectionCount",
                connectionCount.incrementAndGet(),
                Instant.now().toString()
        );

        String seasonJsonMessage = objectMapper.writeValueAsString(seasonDto);
        String connectionCountJsonMessage = objectMapper.writeValueAsString(connectionCountDto);

        session.sendMessage(new TextMessage(seasonJsonMessage));
        allSessionsSendMessage(connectionCountJsonMessage);
        System.out.println("새로운 연결 : " + session.getId());
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("받은 메시지 : " + payload);

        SeasonDto incomingDto = objectMapper.readValue(payload, SeasonDto.class);

        if (!"changeRequest".equals(incomingDto.getType())) {
            System.out.println("잘못된 메시지 타입: " + incomingDto.getType());
            return;
        }

        currentSeason = incomingDto.getSeason();

        SeasonDto dto = new SeasonDto(
                "seasonUpdate",
                currentSeason,
                Instant.now().toString()
        );

        String jsonMessage = objectMapper.writeValueAsString(dto);

        allSessionsSendMessage(jsonMessage);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        sessions.remove(session);

        ConnectionCountDto connectionCountDto = new ConnectionCountDto(
                "connectionCount",
                connectionCount.decrementAndGet(),
                Instant.now().toString()
        );

        String connectionCountJsonMessage = objectMapper.writeValueAsString(connectionCountDto);
        allSessionsSendMessage(connectionCountJsonMessage);
        System.out.println("연결 종료 : " + session.getId());
    }

    private void allSessionsSendMessage(String message) throws Exception {
        sessions.parallelStream().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    logger.error("메시지 전송 실패: {}", e.getMessage(), e);
                }
            }
        });
    }
}
