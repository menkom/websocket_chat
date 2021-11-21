package info.mastera.websocketchat.controller;

import info.mastera.websocketchat.model.ChatMessage;
import info.mastera.websocketchat.service.UserSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Optional;

/**
 * В классе методы, отвечающие за получение сообщения от одного клиента и трансляцию его всем остальным.
 * Добавление пользователя и его сообщения транслируются всем, кто подключен к чату
 * <p>
 * В конфигурации мы указали, что все сообщения от клиентов, направленные по адресу,
 * начинающемуся с /app, будут перенаправлены в соответствующие методы.
 * Имелись в виду как раз методы, аннотированные @MessageMapping.
 */
@Slf4j
@Controller
public class WebsocketChatController {

    @Autowired
    private UserSessionService userSessionService;

    /**
     * Сообщение, направленное по адресу /app/chat.sendMessage будет перенаправлено в метод sendMessage().
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        log.info("Message send : " + chatMessage);
        return chatMessage;
    }

    /**
     * Сообщение, направленное по адресу/app/chat.addUser будет перенаправлено в метод addUser().
     */
    // Сообщение приходит на адрес /app/chat.addUser , где "app" это setApplicationDestinationPrefixes, а "chat.addUser" это то, что именно звесь и прописано
    @MessageMapping("/chat.addUser")
    // Сформированный объект отправляется в "/topic/public", где "/topic" это топик брокера, прописанного в enableSimpleBroker
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor,
                               @Header("simpSessionId") String sessionId) {
        log.info("New user connected : " + chatMessage.getSender());
        // Add username in web socket session
        log.info(
                "SessionId-username:" +
                        sessionId + "|" +
                        headerAccessor.getHeader("simpSessionId") + "|" +
                        chatMessage.getSender()
        );
        Optional.ofNullable(headerAccessor.getSessionAttributes()).
                ifPresent(attributes -> attributes.put("username", chatMessage.getSender()));

        userSessionService.add(chatMessage.getSender(), sessionId);
        return chatMessage;
    }
}
