package info.mastera.websocketchat.controller;

import info.mastera.websocketchat.service.OutgoingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/disconnect")
public class DisconnectController {

    @Autowired
    private OutgoingService outgoingService;

    /**
     * Обратиться http://localhost:8082/disconnect?user=U для отправки сообщения для отключения всех подключений пользователя U
     */
    @GetMapping
    public void disconnect(@RequestParam String username) {
        log.info("Command to disconnect " + username);
        outgoingService.sendMessage(username);
    }
}
