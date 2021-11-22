package info.mastera.websocketchat.listener;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import info.mastera.websocketchat.model.CommandMessage;
import info.mastera.websocketchat.model.CommandTopicMessage;
import info.mastera.websocketchat.service.UserSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommandEventListener implements MessageListener<CommandTopicMessage> {

    @Autowired
    private SimpMessagingTemplate messageSendingOperations;
    @Autowired
    private UserSessionService userSessionService;

    @Override
    public void onMessage(Message<CommandTopicMessage> event) {
        log.info("Hazelcast Command Topic Listener invoked");
        CommandTopicMessage message = event.getMessageObject();

        // Метод convertAndSendToUser добавляет префикс /user и recipientId к адресу /message.
        // Конечный адрес будет выглядеть так /user/{recipientId}/message.
        // Чтобы это работало, надо предварительно
        // 1. Зарегистрировать очередь /user в конфигураторе в методе enableSimpleBroker
        // 2. Подписать пользователя на канал /user/{user_session_Id}/message
        messageSendingOperations.convertAndSendToUser(
                message.getSessionId(),
                "/message",
                new CommandMessage()
                        .setType(message.getType())
        );
        userSessionService.removeSessionId(message.getUsername(), message.getSessionId());
        log.info("Command sent to client");
    }
}
