package info.mastera.websocketchat.service;

import info.mastera.websocketchat.model.ChatMessage;
import info.mastera.websocketchat.model.MessageType;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OutgoingService {

    @Autowired
    private SimpMessagingTemplate messageSendingOperations;
    @Autowired
    private UserSessionService userSessionService;

    @Scheduled(fixedRate = 20000)
    void sendPeriodicMessages() {
        messageSendingOperations.convertAndSend(
                "/topic/public",
                new ChatMessage()
                        .setContent("Periodic message text. " + LocalDateTime.now())
                        .setType(MessageType.COMMAND)
        );
        log.info("Method sendPeriodicMessages");
    }

    public void sendMessage(String username) {
        // Метод convertAndSendToUser добавляет префикс /user и recipientId к адресу /message.
        // Конечный адрес будет выглядеть так /user/{recipientId}/message.
        // Чтобы это работало, надо предварительно
        // 1. Зарегистрировать очередь /user в конфигураторе в методе enableSimpleBroker
        // 2. Подписать пользователя на канал /user/{user_session_Id}/message
        for (String sessionId : userSessionService.getSessions(username)) {
            messageSendingOperations.convertAndSendToUser(
                    sessionId,
                    "/message",
                    new ChatMessage()
                            .setContent("Message from controller")
                            .setType(MessageType.COMMAND)
                            .setSender(sessionId)
            );
        }
        userSessionService.removeUser(username);
        log.info("Method sendMessage");
    }
}
