package info.mastera.websocketchat.service;

import info.mastera.websocketchat.model.CommandTopicMessage;
import info.mastera.websocketchat.model.CommandType;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OutgoingService {

    @Autowired
    private CommandEventService commandEventService;
    @Autowired
    private UserSessionService userSessionService;

    public void sendMessage(String username) {
// В моём случае может быть несколько подключений для одного и того же пользователя.
// Если есть ограничение на количество подключений, то надо добавлять проверку при подключении
// и можно поменять логику рассылки сообщений
        for (String sessionId : userSessionService.getSessions(username)) {
            commandEventService.publish(
                    new CommandTopicMessage()
                            .setUsername(username)
                            .setSessionId(sessionId)
                            .setType(CommandType.LOGOUT)
            );
        }
    }
}
