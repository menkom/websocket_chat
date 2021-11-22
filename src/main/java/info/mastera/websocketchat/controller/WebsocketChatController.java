package info.mastera.websocketchat.controller;

import info.mastera.websocketchat.model.InfoMessage;
import info.mastera.websocketchat.service.UserSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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
     * Сообщение, направленное по адресу/app/newConnection будет перенаправлено в метод addUser().
     */
    // Сообщение приходит на адрес /app/newConnection , где "app" это setApplicationDestinationPrefixes, а "newConnection" это то, что именно звесь и прописано
    @MessageMapping("/newConnection")
    public void addUser(@Payload InfoMessage message,
                               SimpMessageHeaderAccessor headerAccessor,
                               @Header("simpSessionId") String sessionId) {
        log.info("New connection : " + message.getUsername());
        // Add username in web socket session
        log.info(
                "SessionId-username:" +
                        sessionId + "|" +
                        message.getUsername()
        );
        Optional.ofNullable(headerAccessor.getSessionAttributes()).
                ifPresent(attributes -> attributes.put("username", message.getUsername()));

        userSessionService.add(message.getUsername(), sessionId);
    }
}
