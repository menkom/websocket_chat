package info.mastera.websocketchat.controller;

import info.mastera.websocketchat.service.UserSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Optional;

/**
 * Здесь мы слушаем события соединения с сервером и отсоединения.
 * Это нужно для того, чтобы логировать эти события и передавать в чат на всеобщее обозрение.
 * Так все видят, когда кто-то заходит в чат и выходит из него.
 */
@Slf4j
@Component
public class WebSocketEventListener {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;
    @Autowired
    private UserSessionService userSessionService;

    /**
     * Вход в чат мы уже обрабатываем в методе addUser() в ChatController,
     * так что в SessionConnectedEvent ничего делать не нужно.
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("UserLogin:" + event);
        log.info("sessionId:" + event.getMessage().getHeaders().get("simpSessionId"));
        log.info("Received a new web socket connection");
    }

    /**
     * В SessionDisconnectEvent мы извлекаем имя пользователя из сессии и транслируем его всем.
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = Optional.ofNullable( headerAccessor.getSessionAttributes())
                .map(attributes -> (String) attributes.get("username"))
                .orElse(null);

        if (username != null) {
            log.info("User Disconnected : " +
                    event.getSessionId() + " | " +
                    username + " | " +
                    (
                            event.getCloseStatus().getCode() == CloseStatus.NORMAL.getCode()
                                    ? "Correct disconnect"
                                    : event.getCloseStatus().toString()
                    )
            );

            userSessionService.removeSessionId(username, event.getSessionId());

//            ChatMessage chatMessage = new ChatMessage();
//            chatMessage.setType(MessageType.LEAVE);
//            chatMessage.setSender(username);
//
//            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}
